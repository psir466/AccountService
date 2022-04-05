package account.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class PasswordServiceImpl1 implements IPasswordService {

    @Autowired
    PasswordEncoder passwordEncoder;

    public boolean isPasswordInBreachedList(String password, List<String> lstBrPassword)  {
        return lstBrPassword.contains(password);
    }


    public boolean isPasswordAtLeast12Char(String password)  {
        return password.length() >= 12;
    }

    @Override
    public boolean is2PasswordIdentical(String password1, String password2) {


        // Match Raw password (password1) with encoded password (password2)
        return passwordEncoder.matches(password1, password2);
    }
}
