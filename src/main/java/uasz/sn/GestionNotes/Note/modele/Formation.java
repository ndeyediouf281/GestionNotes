package uasz.sn.GestionNotes.Note.modele;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String intitule;
    private String niveau;
    private boolean archive;
    private boolean active;
    // Méthode toString (facultatif)
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Module> modules;
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;
}
