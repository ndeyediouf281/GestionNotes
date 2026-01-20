package uasz.sn.GestionNotes.Utilisateur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Administrateur;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;

import java.util.Optional;

public interface AdministrateurRepository extends JpaRepository<Administrateur,Long> {

    Administrateur findByUsername(String username);

}
