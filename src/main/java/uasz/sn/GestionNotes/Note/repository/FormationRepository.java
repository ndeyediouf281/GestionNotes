package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uasz.sn.GestionNotes.Note.modele.Formation;
@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
//    @Query("SELECT c FROM Classe c WHERE c.formation = ?1")

    boolean existsByIntitule(String nom);

    Formation findByintitule(String nom);
}
