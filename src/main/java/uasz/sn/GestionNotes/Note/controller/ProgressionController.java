package uasz.sn.GestionNotes.Note.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.service.ProgressService;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProgressionController {
    @Autowired
    private UtilisateurService utilisateurService;

    private final EtudiantRepository etudiantRepository;
    private final ProgressService progressService;

    @GetMapping("/progression")
    public String afficherProgression(Authentication authentication, Model model, Principal principal) {
        // Récupérer l'étudiant connecté
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Etudiant etudiant = etudiantRepository.findByUsername(userDetails.getUsername());


        // Calculer les statistiques
        model.addAttribute("progress", progressService.getStudentProgress(etudiant));
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "progression"; // Nom du template Thymeleaf
    }

    // Exception personnalisée
    private static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }
}