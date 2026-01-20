package uasz.sn.GestionNotes.Utilisateur.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Note.modele.Note;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name="id")
@Inheritance(strategy = InheritanceType.JOINED)
public class Etudiant extends Utilisateur {
    private String matricule;
    private boolean archive;
    private String telephone;

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;
    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note> notes; // Liste des notes associées à l'étudiant
    // OU si la relation est via Inscription
    public Formation getFormation() {
        if (!this.inscriptions.isEmpty()) {
            return this.inscriptions.get(0).getFormation(); // Adaptez selon votre modèle
        }
        return null;
    }



}
