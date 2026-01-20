package uasz.sn.GestionNotes.Utilisateur.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Role;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.service.InscriptionService;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;
import uasz.sn.GestionNotes.Utilisateur.service.EtudiantService;

import java.security.Principal;
import java.util.*;

@Controller
public class EtudiantController {
    private final BCryptPasswordEncoder passwordEncoder;
    public EtudiantController(EtudiantRepository enseignantRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    @Autowired
    private EtudiantService etudiantService;
    @Autowired
    private EtudiantRepository etudiantRepository;
    @Autowired
    UtilisateurService utilisateurService;
    @Autowired
    InscriptionService inscriptionService;
    @RequestMapping(value = "/Etudiant/Accueil", method = RequestMethod.GET)
    public String accueil_Etudiant(Model model, Principal principal) {
        Etudiant etudiant = getEtudiantConnecte(principal);
        if (etudiant != null) {
            model.addAttribute("etudiant", etudiant);
        } else {
            model.addAttribute("error", principal == null ? "Utilisateur non connecté." : "Etudiant non trouvé.");
        }
        return "template-Etudiant";
    }
    // Méthode utilitaire pour récupérer l'enseignant connecté
    private Etudiant getEtudiantConnecte(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            return etudiantService.findByUsername(username);
        }
        return null;
    }

    @RequestMapping(value = "/etudiants", method = RequestMethod.GET)
    public String listen_Etudiant(Model model,Principal principal) {
        List<Etudiant> etudiants = etudiantService.listerEtudiant();
        model.addAttribute("ListeEtudiants", etudiants);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());// "ListeEtudiants" doit correspondre au nom utilisé dans la vue
        return "etudiant"; // Retourne la vue "etudiant.html"
    }
    public boolean estNumeroValide(String numero) {
        return numero.matches("\\d{2} \\d{3} \\d{2} \\d{2}");
    }
    // Ajouter un étudiant
    @RequestMapping(value = "/ajouterEtudiant", method = RequestMethod.POST)
    public String ajouter_Etudiant(@ModelAttribute Etudiant etudiant, RedirectAttributes redirectAttributes) {
        // Vérification du format du numéro de téléphone
        if (!estNumeroValide(etudiant.getTelephone())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Le numéro doit être au format XX XXX XX XX.");
            return "redirect:/etudiants";
        }
        try {
            String password = passwordEncoder.encode("Passer123");
            etudiant.setPassword(password);
            etudiant.setDateCreation(new Date());
            etudiant.setActive(true);

            Role role = utilisateurService.ajouter_Role(new Role("Etudiant"));
            List<Role> roles = new ArrayList<>();
            roles.add(role);
            etudiant.setRoles(roles);

            etudiantService.ajouterEtudiant(etudiant);
            redirectAttributes.addFlashAttribute("successMessage", "Étudiant ajouté avec succès !");
            return "redirect:/etudiants";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Le login ou le téléphone existe déjà.");
            return "redirect:/etudiants";
        }
    }



    // Modifier un étudiant
    @RequestMapping(value = "/modifierEtudiant", method = RequestMethod.POST)
    public String modifier_Etudiant(Etudiant etudiant) {
        Etudiant etudiantModif = etudiantService.rechercher(etudiant.getId());
        etudiantModif.setMatricule(etudiant.getMatricule());
        etudiantModif.setNom(etudiant.getNom());
        etudiantModif.setPrenom(etudiant.getPrenom());
        etudiantModif.setTelephone(etudiant.getTelephone());
        etudiantModif.setTelephone(etudiant.getUsername());

        etudiantService.modifierEtudiant(etudiantModif);
        return "redirect:/etudiants";
    }


    // Activer un étudiant
    @RequestMapping(value = "/activerEtudiant", method = RequestMethod.POST)
    public String activer_Etudiant(Long id) {
        etudiantService.activer(id);
        return "redirect:/etudiants";
    }

    // Archiver un étudiant
    @RequestMapping(value = "/archiverEtudiant", method = RequestMethod.POST)
    public String archiver_Etudiant(Long id) {
        etudiantService.archiver(id);
        return "redirect:/etudiants";
    }

    // Afficher le formulaire de modification du mot de passe (GET)
    @GetMapping("/EditerPE")
    public String afficherFormulaire(Model model, Principal principal) {
        Etudiant etudiant = getEtudiantConnecte(principal);
        if (etudiant != null) {
            model.addAttribute("etudiant", etudiant);
            model.addAttribute("message", "Bienvenue sur votre espace étudiant !");
        } else {
            model.addAttribute("error", principal == null ? "Utilisateur non connecté." : "Étudiant non trouvé.");
        }
        return "EditerPE";
    }

    // Traiter la soumission du formulaire de modification du mot de passe (POST)
    @PostMapping("/EditerPE")
    public String modifierMotDePasse(
            @RequestParam Long etudiantId,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            // Vérifier que les mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
                return "redirect:/EditerPE";
            }

            // Vérifier la longueur du mot de passe
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 8 caractères.");
                return "redirect:/EditerPE";
            }

            // Vérifier si l'étudiant existe
            Optional<Etudiant> optionalEtudiant = etudiantRepository.findById(etudiantId);
            if (optionalEtudiant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Étudiant non trouvé.");
                return "redirect:/EditerPE";
            }

            // Modifier et sauvegarder le mot de passe (haché)
            Etudiant etudiant = optionalEtudiant.get();
            etudiant.setPassword(passwordEncoder.encode(newPassword));
            etudiantRepository.save(etudiant);

            // Message de succès
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès !");
            return "redirect:/EditerPE";

        } catch (Exception e) {
            // Gestion des erreurs
            redirectAttributes.addFlashAttribute("error", "Une erreur s'est produite : " + e.getMessage());
            return "redirect:/EditerPE";
        }
    }

    @GetMapping("/mes-modules")
    public String afficherModulesEtudiantConnecte(Model model, Principal principal) {
        // Récupérer l'étudiant connecté
        String username = principal.getName();
        Etudiant etudiant = etudiantService.findByUsername(username);

        if (etudiant == null) {
            return "redirect:/erreur?message=Étudiant non trouvé";
        }

        // Récupérer les modules avec l'ID de l'étudiant connecté
        List<Module> modules = inscriptionService.getModulesByEtudiant(etudiant.getId());

        model.addAttribute("etudiant", etudiant);
        model.addAttribute("modules", modules != null ? modules : Collections.emptyList());

        return "listeModules";
    }

}
