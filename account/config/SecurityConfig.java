package account.config;

import account.exception.RestAuthenticationEntryPoint;
import account.service.IPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    RestAuthenticationEntryPoint restAuthenticationEntryPoint;


    @Override
    protected void configure(AuthenticationManagerBuilder auth){
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // BASIC :
       /* http.httpBasic()
                .and()
                .authorizeRequests()
                .anyRequest().authenticated();*/

        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handle auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/auth/changepass")
                .hasAnyRole("USER", "ACCOUNTANT", "ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "/api/empl/payment")
                .hasAnyRole("USER", "ACCOUNTANT")
                .mvcMatchers(HttpMethod.PUT, "/api/acct/payments")
                .hasAnyRole("ACCOUNTANT")
                .mvcMatchers(HttpMethod.POST, "/api/acct/payments")
                .hasAnyRole("ACCOUNTANT")
                .mvcMatchers(HttpMethod.GET, "/api/admin/user")
                .hasAnyRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.DELETE, "/api/admin/user/**")
                .hasAnyRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.PUT, "/api/admin/user/role")
                .hasAnyRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.PUT, "/api/admin/user/access")
                .hasAnyRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "/api/security/events")
                .hasAnyRole("AUDITOR")
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session


    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }


    // permet de personaliser le processus d'interdiction d'accès (dans l'exemple on a personnalisé le message)
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

}
