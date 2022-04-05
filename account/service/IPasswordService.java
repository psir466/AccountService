package account.service;

import java.io.FileNotFoundException;
import java.util.List;

public interface IPasswordService {


    boolean isPasswordInBreachedList(String password, List<String> ls) throws FileNotFoundException;

    boolean isPasswordAtLeast12Char(String password);

    boolean is2PasswordIdentical(String password1, String password2);
}
