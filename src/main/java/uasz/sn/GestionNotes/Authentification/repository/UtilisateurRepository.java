package uasz.sn.GestionNotes.Authentification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {
    @Query("SELECT u from Utilisateur u where u.username = :username")
    Utilisateur findUtilisateurByusername(@Param("username") String username);
    boolean existsByUsername(String username);

}
