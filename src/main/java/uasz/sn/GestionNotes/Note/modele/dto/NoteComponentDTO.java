package uasz.sn.GestionNotes.Note.modele.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoteComponentDTO {
    private String libelleComponent;
    private Double noteCC;
    private Double noteExamen;
    private Double moyenneEC;
}
