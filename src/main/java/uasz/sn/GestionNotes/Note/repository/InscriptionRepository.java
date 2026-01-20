package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;


import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    Optional<Inscription> findByEtudiantIdAndFormationId(Long etudiantId, Long formationId);

    // Trouver une inscription par étudiant et module (utilisé pour la désinscription)
    Optional<Inscription> findByEtudiantIdAndModuleId(Long etudiantId, Long moduleId);
    boolean existsByEtudiantIdAndModuleId(Long etudiantId, Long moduleId);
    @Query("SELECT i FROM Inscription i WHERE i.module.id = :moduleId")
    List<Inscription> findByModuleId(@Param("moduleId") Long moduleId);
    @Query("SELECT DISTINCT i.etudiant FROM Inscription i WHERE i.formation.id = :formationId")
    List<Etudiant> findByFormationId(@Param("formationId") Long formationId);
    // Trouver toutes les inscriptions par étudiant
    List<Inscription> findByEtudiant(Etudiant etudiant);
    boolean existsByEtudiantIdAndFormationId(Long etudiantId, Long formationId);
    // Inscriptions aux FORMATIONS (sans module)
    List<Inscription> findByFormationIsNotNullAndModuleIsNull();

    // Inscriptions aux MODULES (avec module)
    List<Inscription> findByModuleIsNotNull();



}
