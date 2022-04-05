package account.repository;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
public class PayRoll {

    // Permet d'avoir des séquences d'incréments unique par Table
    @Id
    @GeneratedValue(
            strategy= GenerationType.IDENTITY,
            generator="native"
    )
    @GenericGenerator(
            name = "native",
            strategy = "native"
    )
    ///////////////////////////////
    private Long id;

    @ManyToOne
    @JoinColumn( name = "id_utilisateur" )
    private Utilisateur utilisateur;
    private Double salary;

    @Column(
            columnDefinition = "int"
    )
    @Convert(
            converter = YearMonthIntegerAttributeConverter.class
    )
    private YearMonth salaryPeriod;

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public YearMonth getSalaryPeriod() {
        return salaryPeriod;
    }

    public void setSalaryPeriod(YearMonth salaryPeriod) {
        this.salaryPeriod = salaryPeriod;
    }

    public String formatSalary(){

       String str = Double.toString(this.salary);

       String[] tab = str.split("\\.");

       return String.format("%s dollar(s) %s cent(s)", tab[0], tab[1]);
    }

    public String formatPeriod(){

        Month month = this.salaryPeriod.getMonth();

        return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "-" + this.salaryPeriod.getYear();
    }
}
