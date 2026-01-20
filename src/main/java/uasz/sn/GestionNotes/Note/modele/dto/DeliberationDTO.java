package uasz.sn.GestionNotes.Note.modele.dto;

import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

public class DeliberationDTO {
    private Etudiant etudiant;
    private Double moyenne;
    private String mention;
    private int totalCredits; // Nouveau champ
    private boolean admis;    // Nouveau champ

    // Getters et Setters
    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public Double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(Double moyenne) {
        this.moyenne = moyenne;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public boolean isAdmis() {
        return admis;
    }

    public void setAdmis(boolean admis) {
        this.admis = admis;
    }
}