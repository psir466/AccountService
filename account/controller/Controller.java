package account.controller;

import account.logic.CustomException;
import account.logic.IBusinessLogicPayRoll;
import account.model.*;
import account.repository.PayRollRepository;
import account.repository.Role;
import account.repository.Utilisateur;
import account.repository.UtillisateurRepository;
import account.service.IPasswordService;
import account.service.OperationLoggerService;
import account.service.UserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.util.*;


@RestController
@Validated
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    @Autowired
    Converter converter;

    @Autowired
    UtillisateurRepository utillisateurRepository;

    @Autowired
    UserDetailService userDetailService;

    @Autowired
    PayRollRepository payRollRepository;

    @Autowired
    IPasswordService passwordService;

    @Autowired
    List<String> lstBrPassword;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    IBusinessLogicPayRoll businessLogicPayRoll;

    @Autowired
    OperationLoggerService operationLoggerService;

    @PostMapping("api/auth/signup")
    public UtilisateurDAOWithoutPWD signUp(@Valid @RequestBody UtilisateurDAOWithPWD utilisateurDAOWithPWD) {

        String username = utilisateurDAOWithPWD.getEmail().toLowerCase(Locale.ROOT);

        if (userDetailService.isEmailUtilisateurAlreadyExist(username)) {


            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");

        } else {

            try {
                if (passwordService.isPasswordInBreachedList(utilisateurDAOWithPWD.getPassword(), lstBrPassword)) {

                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
                } else {

                    Utilisateur utilisateur = converter.convertUilisateurDAOWithPWDToUtilisateur(utilisateurDAOWithPWD);

                    if (utillisateurRepository.count() == 0) {

                        utilisateur.becomesAdministrator();

                    } else {
                        utilisateur.becomesUser();
                    }

                    utilisateur = utillisateurRepository.save(utilisateur);

                    LOGGER.info("CREATE_USER Anonymous " + utilisateur.getEmail() + " /api/auth/signup");

                    return converter.convertUilisateurToUtilisateurDAOWithoutPWD(utilisateur);
                }
            } catch (FileNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "");
            }

        }
    }

    @GetMapping("api/empl/payment")
   /* public UtilisateurDAOWithoutPWD emplPayment() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);

        LOGGER.info("************EmplPa*********** {}", username);

        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(username);

        if (utilisateur.isPresent()) {

            return converter.convertUilisateurToUtilisateurDAOWithoutPWD(utilisateur.get());

        } else {

            LOGGER.info("************EmplPa FORB*********** {}", username);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

    }*/

    // On peut renvoyer un Objet indéfini et permet de renvoyer un liste ou un unique objet
    // Apparemment il faut utiliser en réponse : ResponseEntity<Object> ... Euh non pas sur à voir !!!
    public Object emplPayment(@RequestParam Map<String, String> allParams) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);

        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(username);

        if (utilisateur.isPresent()) {

            if (allParams == null) {

                if (payRollRepository.findByEmail(utilisateur.get().getEmail()).isPresent()) {

                    return converter.convertListPayRollD(payRollRepository.findByEmail(utilisateur.get().getEmail()).get());
                } else {
                    return null;
                }
            }

            if (allParams.size() == 0) {
                if (payRollRepository.findByEmail(utilisateur.get().getEmail()).isPresent()) {

                    return converter.convertListPayRollD(payRollRepository.findByEmail(utilisateur.get().getEmail()).get());
                } else {
                    return null;
                }
            }

            if (allParams.size() > 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter invalid");
            }

            if (allParams.size() == 1 && !allParams.containsKey("period")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter invalid");
            }

            if (allParams.size() == 1 && allParams.containsKey("period")) {

                String[] str = allParams.get("period").split("-");

                String m = str[0];

                try {

                    businessLogicPayRoll.checkPeriod(allParams.get("period"));

                    return converter.getPayRollDTODByEmailPeriod(utilisateur.get().getEmail(),
                            allParams.get("period"));

                } catch (DateTimeException d) {

                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value for MonthOfYear (valid values 1 - 12): " + m);

                }

            }


        } else {


            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return null;
    }

   /* @GetMapping("api/empl/payment")
    public List<PayRollDTOD> emplPayment() {

        List<PayRollDTOD> l = new ArrayList<>();

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);

        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(username);

        if (utilisateur.isPresent()) {

            if (payRollRepository.findByEmail(utilisateur.get().getEmail()).isPresent()) {

                return converter.convertListPayRollD(payRollRepository.findByEmail(utilisateur.get().getEmail()).get());
            } else {
                return null;
            }

        } else {


            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }


    }*/

    @PostMapping("api/auth/changepass")
    public EmailStatusDTO changePassword(@RequestBody Map<String, String> new_passwordJson) {

        String new_password = new_passwordJson.get("new_password");


        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);


        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(username.toLowerCase(Locale.ROOT));

        if (utilisateur.isPresent()) {

            try {
                if (passwordService.isPasswordInBreachedList(new_password, lstBrPassword)) {

                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
                } else {

                    if (passwordService.isPasswordAtLeast12Char(new_password)) {

                        if (passwordService.is2PasswordIdentical(new_password, utilisateur.get().getPassword())) {

                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");

                        } else {

                            utilisateur.get().setPassword(passwordEncoder.encode(new_password));

                            utillisateurRepository.save(utilisateur.get());

                            EmailStatusDTO emst = new EmailStatusDTO();

                            emst.setEmail(utilisateur.get().getEmail());
                            emst.setStatus("The password has been updated successfully");

                            LOGGER.info("CHANGE_PASSWORD " + username.toLowerCase(Locale.ROOT) + " "
                                    + utilisateur.get().getEmail() + " api/auth/changepass");

                            return emst;
                        }

                    } else {

                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");

                    }

                }
            } catch (FileNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "");
            }

        } else {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }


    }

    @PostMapping("api/acct/payments")
    public String loadPayRoll(@RequestBody List<PayRollDTO> listPayRollDTO) {

        String resultat = businessLogicPayRoll.checkPayRollList(listPayRollDTO);


        if (resultat.length() == 0) {
            try {

                businessLogicPayRoll.upLoadPayRoll(listPayRollDTO);

                return "{\"status\": \"Added successfully!\"}";

            } catch (CustomException e) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

            }
        } else {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resultat);
        }

    }

    @PutMapping("api/acct/payments")
    public String updateOnePayRoll(@RequestBody PayRollDTO payRollDTO) {

        List<PayRollDTO> listPayRollDTO = new ArrayList<>();

        listPayRollDTO.add(payRollDTO);

        String resultat = businessLogicPayRoll.checkPayRollList(listPayRollDTO);

        if (resultat.length() == 0) {
            try {

                businessLogicPayRoll.upLoadPayRoll(listPayRollDTO);

                return "{\"status\": \"Updated successfully!\"}";

            } catch (CustomException e) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

            }
        } else {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, resultat);
        }

    }

    @GetMapping("api/admin/user")
    public List<UtilisateurDAOWithoutPWD> getUtilisateur() {

        return converter.getAllUser();
    }


    @DeleteMapping("api/admin/user/{email}")
    public String deleteUser(@PathVariable String email) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);


        Optional<Utilisateur> utilisateur = utillisateurRepository.findByEmail(email.toLowerCase(Locale.ROOT));

        if (utilisateur.isPresent()) {

            if (utilisateur.get().isAdministrator()) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");

            } else {

                utillisateurRepository.delete(utilisateur.get());

                LOGGER.info("DELETE_USER " + username + " "
                        + email.toLowerCase(Locale.ROOT) + " "
                        + "api/admin/user");

                return "{\"user\":" + "\"" + email + "\"" + "," + "\"status\": \"Deleted successfully!\"}";
            }

        } else {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

    }

    @PutMapping("api/admin/user/role")
    public UtilisateurDAOWithoutPWD updateRoleUser(@RequestBody UtilisateurDTORole utilisateurDTORole) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);

        Optional<Utilisateur> utilisateur = utillisateurRepository
                .findByEmail(utilisateurDTORole.getUser().toLowerCase(Locale.ROOT));

        if (utilisateur.isPresent()) {

            String updatedRole = "ROLE_" + utilisateurDTORole.getRole();

            Optional<Role> roleRec = Arrays.stream(Role.values()).filter(r -> r.name().equals(updatedRole)).findFirst();

            if (roleRec.isPresent()) {

                if (utilisateurDTORole.getOperation().equals("REMOVE")) {


                    Optional<Role> roleRecUt = utilisateur.get().getRoles().stream()
                            .filter(r -> r.name()
                                    .equals(updatedRole)).findFirst();

                    if (roleRecUt.isPresent()) {

                        if (roleRecUt.get().name().equals("ROLE_ADMINISTRATOR")) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                        }

                        if (utilisateur.get().getRoles().size() > 1) {

                            utilisateur.get().getRoles().remove(roleRecUt.get());


                            utillisateurRepository.save(utilisateur.get());

                            LOGGER.info("REMOVE_ROLE " + username + " "
                                    + roleRec.get().name() + " "
                                    + utilisateurDTORole.getUser().toLowerCase(Locale.ROOT) + " "
                                    + "api/admin/user/role");

                            return converter.convertUilisateurToUtilisateurDAOWithoutPWD(utilisateur.get());

                        } else {

                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");

                        }


                    } else {

                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                    }


                } else {

                    if (utilisateurDTORole.getOperation().equals("GRANT")) {

                        if (utilisateur.get().isAdministrator() &&
                                (roleRec.get().name().equals("ROLE_USER") ||
                                        roleRec.get().name().equals(("ROLE_ACCOUNTANT")) ||
                                                roleRec.get().name().equals(("ROLE_AUDITOR")))) {


                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
                        }

                        if (utilisateur.get().isBusinessRole() &&
                                roleRec.get().name().equals(("ROLE_ADMINISTRATOR"))) {


                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
                        }

                        if (!utilisateur.get().getRoles().contains(roleRec.get())) {


                            utilisateur.get().getRoles().add(roleRec.get());

                        }

                        utillisateurRepository.save(utilisateur.get());

                        LOGGER.info("GRANT_ROLE " + username + " "
                                + roleRec.get().name() + " "
                                + utilisateurDTORole.getUser().toLowerCase(Locale.ROOT) + " "
                                + "api/admin/user/role");


                        return converter.convertUilisateurToUtilisateurDAOWithoutPWD(utilisateur.get());

                    }

                }

            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
            }

        } else {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }


        return null;
    }

    @PutMapping("api/admin/user/access")
    public String updateRoleUser(@RequestBody UtilisateurDTOUNLCKLCK utilisateurDTOUNLCKLCK) {

        String operation = null;

        String username = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase(Locale.ROOT);

        Optional<Utilisateur> utilisateur = utillisateurRepository
                .findByEmail(utilisateurDTOUNLCKLCK.getUser().toLowerCase(Locale.ROOT));

        if (utilisateur.isPresent()) {


            if (utilisateurDTOUNLCKLCK.getOperation().equals("LOCK")) {

                operation = "locked";

                if (utilisateur.get().isAdministrator()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
                } else {
                    utilisateur.get().lockUser();
                }
            }

            if (utilisateurDTOUNLCKLCK.getOperation().equals("UNLOCK")) {
                operation = "unlocked";
                utilisateur.get().unlockUser();
            }

            LOGGER.info(utilisateurDTOUNLCKLCK.getOperation() + "_USER " + username + " "
                    + utilisateurDTOUNLCKLCK.getUser().toLowerCase(Locale.ROOT) + " " + "api/admin/user/access");


            utillisateurRepository.save(utilisateur.get());

            return "{\"status\": \"User " + utilisateurDTOUNLCKLCK.getUser().toLowerCase(Locale.ROOT)
                    + " " + operation +
                    "!\"}";
        }

        return null;

    }

    @GetMapping("api/security/events")
    public List<SecurityEventDTO> getSecurityEvent() throws FileNotFoundException {

        return operationLoggerService.getListSecurityEvents();
    }

}


