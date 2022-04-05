package account.model;

import account.repository.Utilisateur;

import java.time.YearMonth;

public class PayRollDTO {

    private String employee;
    private String period;
    private String  salary;


    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
