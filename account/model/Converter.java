package account.model;

import account.logic.CustomException;
import account.model.UtilisateurDAOWithoutPWD;
import account.repository.PayRoll;
import account.repository.PayRollRepository;
import account.repository.Utilisateur;
import account.repository.UtillisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Converter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UtillisateurRepository utillisateurRepository;

    @Autowired
    PayRollRepository payRollRepository;


    public Utilisateur convertUilisateurDAOWithPWDToUtilisateur(UtilisateurDAOWithPWD utilsateurDAOWithPWD) {


        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setPassword(passwordEncoder.encode(utilsateurDAOWithPWD.getPassword()));
        utilisateur.setName(utilsateurDAOWithPWD.getName());
        utilisateur.setLastname(utilsateurDAOWithPWD.getLastname());
        utilisateur.setEmail(utilsateurDAOWithPWD.getEmail().toLowerCase(Locale.ROOT));

        return utilisateur;

    }

    public UtilisateurDAOWithoutPWD convertUilisateurToUtilisateurDAOWithoutPWD(Utilisateur utilisateur) {

        UtilisateurDAOWithoutPWD utilisateurDAOWithoutPWD = new UtilisateurDAOWithoutPWD();

        utilisateurDAOWithoutPWD.setName(utilisateur.getName());
        utilisateurDAOWithoutPWD.setLastname(utilisateur.getLastname());
        utilisateurDAOWithoutPWD.setEmail(utilisateur.getEmail());
        utilisateurDAOWithoutPWD.setId(utilisateur.getId());
        utilisateurDAOWithoutPWD.setRoles(utilisateur.getRoles().stream().sorted((r1, r2) ->
                r1.name().compareTo(r2.name())).collect(Collectors.toList())
                );

        return utilisateurDAOWithoutPWD;

    }

    public PayRoll convertPayRollDTOToPayRoll(PayRollDTO payRollDTO) throws CustomException {


        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(payRollDTO.getEmployee());

        if (utilisateur.isPresent()) {

            if (!payRollDTO.getSalary().isEmpty()) {
                if (Integer.parseInt(payRollDTO.getSalary()) >= 0) {

                    String[] periodStr = payRollDTO.getPeriod().split("-");
                    int year = Integer.parseInt(periodStr[1]);
                    int month = Integer.parseInt(periodStr[0]);

                    Optional<PayRoll> pr2 = payRollRepository.findByEmailAndPeriod(payRollDTO.getEmployee(),
                            YearMonth.of(year, month));

                    if (pr2.isPresent()) {

                        pr2.get().setSalary((double) Integer.parseInt(payRollDTO.getSalary()) / 100);

                        return pr2.get();

                    } else {

                        PayRoll pr = new PayRoll();

                        pr.setUtilisateur(utilisateur.get());
                        pr.setSalary((double) Integer.parseInt(payRollDTO.getSalary()) / 100);
                        pr.setSalaryPeriod(YearMonth.of(year, month));

                        return pr;
                    }


                } else {

                    throw new CustomException("Salaire < 0 " + payRollDTO.getEmployee());

                }

            } else {

                throw new CustomException("Salaire empty " + payRollDTO.getEmployee());
            }

        } else {

            throw new CustomException("Utilisateur not found " + payRollDTO.getEmployee());

        }

    }

    public PayRollDTO convertPayRolltoPayRollDTO(PayRoll payRoll){

        PayRollDTO payRollDTO = new PayRollDTO();

        payRollDTO.setEmployee(payRoll.getUtilisateur().getEmail());
        payRollDTO.setPeriod(payRoll.formatPeriod());
        payRollDTO.setSalary(payRoll.formatSalary());

        return payRollDTO;

    }

    public List<PayRollDTO> convertListPayRoll(List<PayRoll> payRollList){

        List<PayRollDTO> lpayRollDto = new ArrayList<>();

        for(PayRoll payRoll : payRollList){

            lpayRollDto.add(convertPayRolltoPayRollDTO(payRoll));
        }

        return lpayRollDto;
    }

    public PayRollDTO getPayRollDTOByEmailPeriod(String email, String period){

        String[] periodStr = period.split("-");
        int year = Integer.parseInt(periodStr[1]);
        int month = Integer.parseInt(periodStr[0]);

        Optional<PayRoll> pr = payRollRepository.findByEmailAndPeriod(email,
                YearMonth.of(year, month));

        return convertPayRolltoPayRollDTO(pr.get());
    }


    public PayRollDTOD convertPayRolltoPayRollDTOD(PayRoll payRoll){

        PayRollDTOD payRollDTOD= new PayRollDTOD();

        payRollDTOD.setName(payRoll.getUtilisateur().getName());
        payRollDTOD.setLastname(payRoll.getUtilisateur().getLastname());
        payRollDTOD.setPeriod(payRoll.formatPeriod());
        payRollDTOD.setSalary(payRoll.formatSalary());

        return payRollDTOD;

    }

    public List<PayRollDTOD> convertListPayRollD(List<PayRoll> payRollList){

        List<PayRoll> payRollListSorted = payRollList.stream()
                .sorted(Comparator.comparing(PayRoll::getSalaryPeriod).reversed())
                .collect(Collectors.toList());

        List<PayRollDTOD> lpayRollDtod = new ArrayList<>();

        for(PayRoll payRoll : payRollListSorted){

            lpayRollDtod.add(convertPayRolltoPayRollDTOD(payRoll));
        }

        return lpayRollDtod;
    }

    public PayRollDTOD getPayRollDTODByEmailPeriod(String email, String period){

        String[] periodStr = period.split("-");
        int year = Integer.parseInt(periodStr[1]);
        int month = Integer.parseInt(periodStr[0]);

        Optional<PayRoll> pr = payRollRepository.findByEmailAndPeriod(email,
                YearMonth.of(year, month));

        return convertPayRolltoPayRollDTOD(pr.get());
    }

    public List<UtilisateurDAOWithoutPWD> getAllUser(){

        List<UtilisateurDAOWithoutPWD> l = new ArrayList<>();

        for(Utilisateur utilisateur : utillisateurRepository.findAll()
                .stream()
                .sorted((u1, u2) ->  u1.getId().compareTo(u2.getId()))
                .collect(Collectors.toList())) {
            l.add(convertUilisateurToUtilisateurDAOWithoutPWD(utilisateur));
        }

        return l;
    }




}
