package uasz.sn.GestionNotes.Utilisateur.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.dto.ReleveNotesDTO;
import uasz.sn.GestionNotes.Note.service.DeliberationService;
import uasz.sn.GestionNotes.Utilisateur.model.Administrateur;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.AdministrateurRepository;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class AdministrateurController {
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private AdministrateurRepository administrateurRepository;
    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private DeliberationService deliberationService;


    @RequestMapping(value = "/Administrateur/Accueil", method = RequestMethod.GET)
    public String accueil_Administrateur(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());
        return "Administrateur"; // Retourner la vue Administrateur
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @GetMapping("/releve/selectionner")
    public String afficherPageReleve(Model model) {
        model.addAttribute("etudiants", etudiantRepository.findAll());
        return "releve";
    }

    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @PostMapping("/releve")
    public String genererReleve(@RequestParam Long etudiantId, Model model) {
        model.addAttribute("etudiants", etudiantRepository.findAll());
        model.addAttribute("releve", deliberationService.genererReleveNotes(etudiantId));
        return "releve";
    }

    // Gère les erreurs système
    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur système: " + ex.getMessage());
    }

    // Gère les erreurs liées à des requêtes invalides
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Requête invalide: " + ex.getMessage());
    }

    @GetMapping("/AffichageListes")
    public String showDashboard(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "AffichageListes"; // Le nom du fichier HTML sans l'extension
    }


}
