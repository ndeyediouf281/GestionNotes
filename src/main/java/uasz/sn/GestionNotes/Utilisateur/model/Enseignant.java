package uasz.sn.GestionNotes.Utilisateur.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Note.modele.Module; // Correct import for your custom Module entity

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name="id")
@Inheritance(strategy = InheritanceType.JOINED)

public class Enseignant extends Utilisateur {
    private String matricule;
    private String specialite;
    private String grade;
    private boolean archive;
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL)
    private List<Module> modules;



}
