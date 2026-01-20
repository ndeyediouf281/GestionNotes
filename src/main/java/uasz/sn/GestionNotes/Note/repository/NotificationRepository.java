package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uasz.sn.GestionNotes.Note.modele.Notification;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEtudiantAndLueFalseOrderByDateCreationDesc(Etudiant etudiant);
    int countByEtudiantAndLueFalse(Etudiant etudiant);
    // Récupère toutes les notifications d'un étudiant triées par date
    List<Notification> findByEtudiantOrderByDateCreationDesc(Etudiant etudiant);


}