package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.Module; // Importez votre classe Module
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    boolean existsByCodeAndLibelle(String code, String libelle);

    boolean existsByCode(String code);

    List<Module> findByFormation(Formation formation);

    boolean existsByLibelle(String libelle);

    Optional<Module> findById(Long id);

    List<Module> findByEnseignant(Enseignant enseignant);
    @Query("SELECT DISTINCT n.module FROM Note n " +
            "WHERE n.etudiant.id = :etudiantId " +
            "AND n.moyenne >= 10")
    List<Module> findModulesReussis(@Param("etudiantId") Long etudiantId);


}