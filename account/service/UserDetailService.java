package account.service;

import account.listener.AuthentificationFailureListener;
import account.repository.Utilisateur;
import account.repository.UtillisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class UserDetailService implements UserDetailsService {

    public static final int MAX_FAILED_ATTEMPTS = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailService.class);

    @Autowired
    private UtillisateurRepository utillisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Optional<Utilisateur> user = utillisateurRepository.findByEmail(username.toLowerCase(Locale.ROOT));

        if (user.isPresent()){

            return user.get();
        }else{


            throw new UsernameNotFoundException(String.format("  "));
        }
    }

    public boolean isEmailUtilisateurAlreadyExist(String email) {

        boolean alreadyExist = true;

        System.out.println(email);

        Optional<Utilisateur> user = utillisateurRepository.findByEmail(email.toLowerCase(Locale.ROOT));

        if(user.isEmpty()){
            alreadyExist = false;
        }

        return alreadyExist;
    }

    public void increaseFailedAttempts(String email) {

        Optional<Utilisateur> utiDatabase = utillisateurRepository.findByEmail(email);

        if(utiDatabase.isPresent()){

            int failAttempts = utiDatabase.get().getFailedAttempt();

            if(failAttempts >= MAX_FAILED_ATTEMPTS - 1){

                LOGGER.info("BRUTE_FORCE " + email + " "
                        + request.getRequestURI() + " " + request.getRequestURI());

                if(!utiDatabase.get().isAdministrator()) {
                    lock(email);
                }


            }else {

                utiDatabase.get().setFailedAttempt(failAttempts + 1);

            }

            utillisateurRepository.save(utiDatabase.get());

        }

    }

    public void resetFailedAttempts(String email) {

        Optional<Utilisateur> utiDatabase = utillisateurRepository.findByEmail(email);

        if(utiDatabase.isPresent()){

            utiDatabase.get().setFailedAttempt(0);

            utillisateurRepository.save(utiDatabase.get());

        }
    }

    public void lock(String email) {

        Optional<Utilisateur> utiDatabase = utillisateurRepository.findByEmail(email);

        if(utiDatabase.isPresent()){

            utiDatabase.get().setAccountNonLocked(false);

            utillisateurRepository.save(utiDatabase.get());


            LOGGER.info("LOCK_USER " + email + " " +
                    email + " " + request.getRequestURI());

        }
    }


}
