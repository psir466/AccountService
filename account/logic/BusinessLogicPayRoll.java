package account.logic;

import account.model.Converter;
import account.model.PayRollDTO;
import account.repository.PayRoll;
import account.repository.PayRollRepository;
import account.repository.Utilisateur;
import account.repository.UtillisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessLogicPayRoll implements IBusinessLogicPayRoll {

    @Autowired
    Converter converter;

    @Autowired
    PayRollRepository payRollRepository;

    @Autowired
    UtillisateurRepository utillisateurRepository;

    private StringBuilder sb = new StringBuilder();

    private List<UtilsateurPeriod> lu = new ArrayList<>();

    @Override
    @Transactional
    public void upLoadPayRoll(List<PayRollDTO> lPRDTO) throws CustomException {

        for (PayRollDTO payRollDTO : lPRDTO) {

            PayRoll pr = converter.convertPayRollDTOToPayRoll(payRollDTO);

            payRollRepository.save(pr);
        }

    }


    @Override
    public String checkPayRollList(List<PayRollDTO> lPRDTO) {

        this.lu.clear();


        if (sb.length() > 0) {
            sb.delete(0, sb.length());
        }

        int index = 1;

        boolean notFirstAno = false;

        for (PayRollDTO payRollDTO : lPRDTO) {

            String[] erreur = this.checkPayRoll(payRollDTO);

            if (erreur[0].equals("PAYMENT")) {
                if (notFirstAno) {
                    sb.append(", ");
                }
                sb.append(String.format("payment[%d].employee must exist", index));
                notFirstAno = true;
            }

            if (erreur[1].equals("SALARY")) {
                if (notFirstAno) {
                    sb.append(", ");
                }
                sb.append(String.format("payments[%d].salary: Salary must be non negative!", index));
                notFirstAno = true;
            }

            if (erreur[2].equals("YEARMONTH")) {
                if (notFirstAno) {
                    sb.append(", ");
                }
                sb.append(String.format("payments[%d].period: Wrong date!", index));
                notFirstAno = true;
            }

            UtilsateurPeriod utilsateurPeriod = new UtilsateurPeriod(payRollDTO.getEmployee(),
                    payRollDTO.getPeriod());

            if (this.lu.contains(utilsateurPeriod)) {

                if (notFirstAno) {
                    sb.append(", ");
                }
                sb.append(String.format("payments[%d].period already exist", index));
                notFirstAno = true;

            } else {
                this.lu.add(utilsateurPeriod);
            }


            index++;

        }

        return sb.toString();
    }

    @Override
    public String[] checkPayRoll(PayRollDTO payRollDTO) {

        String[] erreur = {"", "", ""};

        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(payRollDTO.getEmployee());

        if (!utilisateur.isPresent()) {
            erreur[0] = "PAYEMENT";
        }

        if (payRollDTO.getSalary().isEmpty()) {
            erreur[1] = "SALARY";
        }

        if (!payRollDTO.getSalary().isEmpty()) {
            if (Integer.parseInt(payRollDTO.getSalary()) < 0) {

                erreur[1] = "SALARY";
            }
        }

        try {

            checkPeriod(payRollDTO.getPeriod());

        } catch (DateTimeException e) {


            erreur[2] = "YEARMONTH";

        }

      /*  String[] periodStr = payRollDTO.getPeriod().split("-");
        int year = Integer.parseInt(periodStr[1]);
        int month = Integer.parseInt(periodStr[0]);

        try {
            YearMonth.of(year, month);

        } catch (DateTimeException e) {


            erreur[2] = "YEARMONTH";

        }*/

        return erreur;
    }


    @Override
    public void checkPeriod(String period) throws DateTimeException {

        String[] periodStr = period.split("-");
        int year = Integer.parseInt(periodStr[1]);
        int month = Integer.parseInt(periodStr[0]);

        YearMonth.of(year, month);

    }

}
