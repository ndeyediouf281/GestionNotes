package uasz.sn.GestionNotes.Note.modele.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// NoteModuleDTO.java
@Getter
@AllArgsConstructor
public class NoteModuleDTO {
    private String codeModule;
    private String libelleModule;
    private int credits;
    private int coefficient; // Coefficient du module
    private List<NoteComponentDTO> components;
    private Double moyenneModule;
    private String mentionModule;
}