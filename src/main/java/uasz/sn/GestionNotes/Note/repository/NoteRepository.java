package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uasz.sn.GestionNotes.Note.modele.Note;
import uasz.sn.GestionNotes.Note.modele.Module;

import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;


import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n JOIN FETCH n.module m JOIN FETCH m.enseignant e WHERE e.id = :enseignantId")
    List<Note> findByModuleEnseignant(@Param("enseignantId") Long enseignantId);
    List<Note> findByEtudiantIdAndFormationId(Long etudiantId, Long formationId);
    List<Note> findByEtudiantId(Long etudiantId);
    @Query("SELECT AVG(n.moyenne) " +
            "FROM Note n " +
            "JOIN n.etudiant e " +
            "JOIN e.inscriptions i " +
            "WHERE i.formation.id = :formationId")
    Optional<Double> calculateMoyennePromotion(@Param("formationId") Long formationId);


    @Query("SELECT e.id, AVG(n.moyenne) as moyenne " +
            "FROM Note n " +
            "JOIN n.etudiant e " +
            "JOIN e.inscriptions i " +
            "WHERE i.formation.id = :formationId " +
            "GROUP BY e.id " +
            "ORDER BY moyenne DESC")
    List<Object[]> getClassementPromotion(@Param("formationId") Long formationId);


    Note findByEtudiantAndModule(Etudiant etudiant, Module module);
}