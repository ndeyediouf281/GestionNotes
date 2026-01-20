package uasz.sn.GestionNotes.Note.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.dto.DeliberationDTO;
import uasz.sn.GestionNotes.Note.modele.dto.ReleveNotesDTO;
import uasz.sn.GestionNotes.Note.service.DeliberationService;
import uasz.sn.GestionNotes.Note.service.FormationService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class DeliberationController {

    @Autowired
    private DeliberationService deliberationService;

    @Autowired
    private FormationService formationService;
    @Autowired
    UtilisateurService utilisateurService;

    // Affichage initial du formulaire
    @GetMapping("/deliberation")
    public String afficherFormulaireDeliberation(Model model, Principal principal) {
        model.addAttribute("formations", formationService.listerFormations());
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "deliberation";
    }

    @GetMapping(value = "/deliberation", params = "formationId")
    public String lancerDeliberation(@RequestParam Long formationId, Model model,Principal principal) {
        List<DeliberationDTO> deliberations = deliberationService.deliberer(formationId);
        Map<String, Object> stats = deliberationService.calculerStatistiques(deliberations);

        model.addAttribute("deliberations", deliberations);
        model.addAttribute("formations", formationService.listerFormations());
        model.addAttribute("stats", stats);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "deliberation";
    }

}