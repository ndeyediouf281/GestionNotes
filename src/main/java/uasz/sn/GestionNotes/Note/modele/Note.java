package uasz.sn.GestionNotes.Note.modele;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module; // Doit correspondre au n.module de la requête

    private Double noteControleContinu;
    private Double noteExamen;
    private Double moyenne;
    @PrePersist
    @PreUpdate
    // Méthode pour calculer et mettre à jour la moyenne
    public void calculerEtSetMoyenne() {
        this.moyenne = (noteControleContinu * 0.3) + (noteExamen * 0.7);
    }

    // Getter pour la moyenne
    public Double getNote() {
        if (moyenne == null) {
            calculerEtSetMoyenne();
        }
        return moyenne;
    }

    @ManyToOne
    @JoinColumn(name = "deliberation_id")
    private Deliberation deliberation;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", referencedColumnName = "id", nullable = false)
    private Etudiant etudiant;  // L'étudiant auquel la note appartient
}
