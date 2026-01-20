package uasz.sn.GestionNotes.Note.modele;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    private String message;
    private boolean lue;
    private LocalDateTime dateCreation;

    // Getters/Setters
}
