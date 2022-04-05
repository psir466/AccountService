package account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayRollRepository extends JpaRepository<PayRoll, Long> {

    // Attention on fait JQL (pas SQL) du coup il faut se référer aux noms de zones
    // dans le class en partiulier : est pas  userid qui est le nom
    // dans la table !!!!!!!!!!
    @Query(value = "select p from PayRoll p where p.utilisateur.email = :email and salaryPeriod = :period", nativeQuery = false)
    public Optional<PayRoll> findByEmailAndPeriod(@Param("email") String email, @Param("period")
            YearMonth period);


    @Query(value = "select p from PayRoll p where p.utilisateur.email = :email", nativeQuery = false)
    public Optional<List<PayRoll>> findByEmail(@Param("email") String email);
}
