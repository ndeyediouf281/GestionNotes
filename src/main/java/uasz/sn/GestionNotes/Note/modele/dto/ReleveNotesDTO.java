package uasz.sn.GestionNotes.Note.modele.dto;

import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
@Getter
@AllArgsConstructor
public class ReleveNotesDTO {
    private Etudiant etudiant;
    private List<NoteModuleDTO> modules;
    private double moyenneGenerale;
    private int totalCredits; // Nouveau champ
    private String mentionGenerale;
    private boolean admis;
}