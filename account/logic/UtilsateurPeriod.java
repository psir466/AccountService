package account.logic;

import java.util.Objects;

public class UtilsateurPeriod {

    private String employee;
    private String period;

    public UtilsateurPeriod(String employee, String period) {
        this.employee = employee;
        this.period = period;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }


    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilsateurPeriod that = (UtilsateurPeriod) o;
        return employee.equals(that.employee) && period.equals(that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employee, period);
    }
}
