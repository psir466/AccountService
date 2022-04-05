package account.logic;

import account.model.PayRollDTO;
import account.repository.PayRoll;

import java.util.List;

public interface IBusinessLogicPayRoll {

    void upLoadPayRoll(List<PayRollDTO> lPRDTO) throws CustomException;

    String checkPayRollList(List<PayRollDTO> lPRDTO);

    String[] checkPayRoll(PayRollDTO payRollDTO);

    void checkPeriod(String period);

}
