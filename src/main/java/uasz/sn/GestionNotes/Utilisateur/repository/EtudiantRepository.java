package uasz.sn.GestionNotes.Utilisateur.repository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant,Long> {
    Optional<Etudiant> findById(Long id);
    // Récupérer tous les étudiants inscrits à un module spécifique
    Etudiant findByUsername(String username);

}
