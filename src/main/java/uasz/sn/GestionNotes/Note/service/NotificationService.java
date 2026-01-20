package uasz.sn.GestionNotes.Note.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Notification;
import uasz.sn.GestionNotes.Note.repository.NotificationRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void creerNotification(Etudiant etudiant, String message) {
        Notification notification = new Notification();
        notification.setEtudiant(etudiant);
        notification.setMessage(message);
        notification.setLue(false);
        notification.setDateCreation(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsNonLues(Etudiant etudiant) {
        return notificationRepository.findByEtudiantAndLueFalseOrderByDateCreationDesc(etudiant);
    }

    public void marquerCommeLue(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setLue(true);
            notificationRepository.save(notification);
        });
    }
    public List<Notification> getNotifications(Etudiant etudiant) {
        // Récupère toutes les notifications triées par date décroissante
        List<Notification> notifications = notificationRepository.findByEtudiantOrderByDateCreationDesc(etudiant);

        // Garantit de ne jamais retourner null
        return notifications != null ? notifications : Collections.emptyList();
    }
}
