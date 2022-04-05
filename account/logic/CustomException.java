package account.logic;

// on utilise RunTime exception car le RoLLBack dans @Transactional se fait uniquement sur les
// RunTime Exception voir la class BusinessLogicPayRoll
public class CustomException extends RuntimeException{

    public CustomException(String message) {

        super(message);

    }
}
