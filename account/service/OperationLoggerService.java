package account.service;

import account.model.SecurityEventDTO;
import account.repository.Role;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Service
public class OperationLoggerService {

    private  Scanner scanner;

    public List<SecurityEventDTO> getListSecurityEvents(){

        List<SecurityEventDTO> listSecurityEvents = new ArrayList<>();

        File file = new File("C:/TestSpringLogs/account.log");

        int id  = 0;

        try {
            scanner = new Scanner(file);

            while (scanner.hasNext()) {

                String str = scanner.nextLine();

                String[] tab = str.split(" ");

                Optional<OperationLogger> operationLogger = Arrays.stream(OperationLogger.values())
                        .filter(o -> o.name().equals(tab[3]))
                        .findFirst();

                if(operationLogger.isPresent()) {

                    SecurityEventDTO securityEventDTO = new SecurityEventDTO();
                    securityEventDTO.setId(++id);
                    securityEventDTO.setAction(tab[3]);
                    securityEventDTO.setDate(tab[0] + " " + tab[1]);
                    securityEventDTO.setSubject(tab[4]);

                    if(tab[3].equals("GRANT_ROLE") || tab[3].equals("REMOVE_ROLE")){

                        String role = tab[5].substring(5);

                        if(tab[3].equals("GRANT_ROLE")){

                            securityEventDTO.setObject(String.format("Grant role %s to %s", role, tab[6]));

                        }

                        if(tab[3].equals("REMOVE_ROLE")){

                            securityEventDTO.setObject(String.format("Remove role %s from %s", role, tab[6]));

                        }

                        securityEventDTO.setPath(tab[7]);

                    }else {

                        if(tab[3].equals("UNLOCK_USER") || tab[3].equals("LOCK_USER")){

                            if(tab[3].equals("UNLOCK_USER")){
                                securityEventDTO.setObject(String.format("Unlock user %s", tab[5]));
                            }

                            if(tab[3].equals("LOCK_USER")){
                                securityEventDTO.setObject(String.format("Lock user %s", tab[5]));
                            }

                            securityEventDTO.setPath(tab[6]);

                        }else {

                            securityEventDTO.setObject(tab[5]);
                            securityEventDTO.setPath(tab[6]);

                        }
                    }

                    listSecurityEvents.add(securityEventDTO);
                }
            }

            scanner.close();

            return listSecurityEvents;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
