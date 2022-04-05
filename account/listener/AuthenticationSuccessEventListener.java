package account.listener;

import account.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AuthenticationSuccessEventListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserDetailService userDetailService;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent e) {

        String email = e.getAuthentication().getName();

        userDetailService.resetFailedAttempts(email.toLowerCase(Locale.ROOT));
    }
}
