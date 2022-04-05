package account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
public class Config {


    // Spring va créer un bean qui s'appelle passwordencoder contenant l'objet Bcrypt...
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public List<String> listBrPassword() throws FileNotFoundException {

        List<String> ls = new ArrayList<>();

        Scanner scanner = null;

        File file = null;
        file = ResourceUtils.getFile("classpath:brPass.txt");

        scanner = new Scanner(file);

        while (scanner.hasNext()) {

            String str = scanner.nextLine();

            ls.add(str);
        }


        return ls;
    }

}
