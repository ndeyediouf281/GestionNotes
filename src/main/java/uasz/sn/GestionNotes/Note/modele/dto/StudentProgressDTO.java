package uasz.sn.GestionNotes.Note.modele.dto;

import lombok.Data;
import lombok.Getter;

import java.util.Map;
@Data
public class StudentProgressDTO {
    private double moyenneGenerale;
    private double moyennePromotion; // Ajouté
    private String rangClassement;   // Ajouté
    private Map<String, Double> moyenneParModule;
    private int creditsAcquis;
    private int creditsTotaux;
    private double progressionFormation;
}