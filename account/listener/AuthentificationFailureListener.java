package account.listener;

import account.config.CustomAccessDeniedHandler;
import account.service.UserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
public class AuthentificationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthentificationFailureListener.class);

    @Autowired
    HttpServletRequest request;

    @Autowired
    private UserDetailService userDetailService;


    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {


        String email = (String) e.getAuthentication().getPrincipal();

        LOGGER.info("LOGIN_FAILED " + email.toLowerCase(Locale.ROOT) + " "
                + request.getRequestURI() + " " + request.getRequestURI());

        userDetailService.increaseFailedAttempts(email.toLowerCase(Locale.ROOT));


    }
}
