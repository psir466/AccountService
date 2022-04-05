package account.model;

import org.springframework.stereotype.Service;

import javax.validation.constraints.*;

public class UtilisateurDAOWithPWD {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String lastname;

    @NotNull
    @NotBlank
    @Pattern(regexp=".+@acme.com", message = "Email invalide")
    private String email;

    @NotNull
    @NotBlank
    @Size(min=12, message="Password length must be 12 chars minimum!")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
