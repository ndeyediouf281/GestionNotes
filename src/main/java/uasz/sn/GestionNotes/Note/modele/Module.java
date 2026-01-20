package uasz.sn.GestionNotes.Note.modele;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String code;
    private String libelle;
    private Integer credit;
    private Integer vh;
    private Integer coefficient;
    private boolean archive;
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant; // Assurez-vous que cette propriété existe
    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false) // Obligatoire
    private Formation formation;
    @OneToMany(mappedBy = "module")
    private List<Inscription> inscriptions;

}


