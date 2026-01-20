package uasz.sn.GestionNotes.Note.modele;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deliberation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inscription_id")
    private Inscription inscription;

    private Double moyenne;
    private String mention;
    private LocalDate dateDeliberation;

    @OneToMany(mappedBy = "deliberation")
    private List<Note> notes;
    private boolean admis;
    }
