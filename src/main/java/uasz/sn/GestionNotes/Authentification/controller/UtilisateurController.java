package uasz.sn.GestionNotes.Authentification.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Role;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Utilisateur.model.Administrateur;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

@Controller
public class UtilisateurController {
    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Affiche la page de connexion
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {

            // Récupérer l'utilisateur authentifié
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = auth.getAuthorities().toString(); // Récupère le rôle de l'utilisateur

            // Récupérer le rôle de l'utilisateur
            boolean isEtudiant = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ETUDIANT"));
            boolean isEnseignant = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ENSEIGNANT"));
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATEUR"));

            // Redirection selon le rôle
            if (isEtudiant) {
                return "redirect:/Etudiant/Accueil";
            } else if (isEnseignant) {
                return "redirect:/Enseignant/Accueil";
            } else if (isAdmin) {
                return "redirect:/Administrateur/Accueil";
            }

            // Si aucun rôle trouvé, retour à la page de connexion
            return "redirect:/login?error";
        }
    @GetMapping(value = "/logout")
    public String logOutAndRedirectToLoginPage(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/login?logout=true"; // Redirige vers la page de connexion après déconnexion
    }



    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Affiche la page d'inscription
    }


    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam String nom,
            @RequestParam String prenom) {

        System.out.println("Début du processus d'inscription...");

        // Vérifie si le nom d'utilisateur existe déjà
        if (utilisateurService.existsByUsername(username)) {
            System.out.println("Le username existe déjà !");
            return "redirect:/register?error=exists";
        }

        // Vérifie si le rôle existe déjà dans la base de données
        Role userRole = utilisateurService.findRoleByName(role);
        if (userRole == null) {
            userRole = new Role(role);
            utilisateurService.saveRole(userRole);  // Enregistre le rôle s'il n'existe pas
        }

        // Instancier l'utilisateur en fonction du rôle sélectionné
        Utilisateur utilisateur = null;
        switch (role) {
            case "Etudiant":
                utilisateur = new Etudiant();
                break;
            case "Enseignant":
                utilisateur = new Enseignant();
                break;
            case "Administrateur":
                utilisateur = new Administrateur();
                break;
            default:
                return "redirect:/register?error=invalidRole"; // Si le rôle est invalide
        }

        // Affectation des valeurs saisies par l'utilisateur
        utilisateur.setUsername(username);
        utilisateur.setPassword(passwordEncoder.encode(password)); // Encoder le mot de passe
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setRoles(Collections.singletonList(userRole)); // Associer le rôle existant

        // Enregistrer l'utilisateur
        utilisateurService.save(utilisateur);
        System.out.println("Utilisateur enregistré avec succès: " + utilisateur.getUsername());

        return "redirect:/login?register=success";
}
    @RequestMapping(value = "/profil", method = RequestMethod.GET)
    public String profil_Etudiant(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom().charAt(0));
        return "profil";
    }

    @PostMapping("/profil/modifier")
    public String modifierProfil(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirectAttributes) {
        utilisateurService.modifierUtilisateur(utilisateur);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès !");
        return "redirect:/profil";
    }


}
