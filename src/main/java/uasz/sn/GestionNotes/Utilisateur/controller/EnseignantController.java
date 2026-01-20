package uasz.sn.GestionNotes.Utilisateur.controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Role;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.service.InscriptionService;
import uasz.sn.GestionNotes.Note.service.ModuleService;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EnseignantRepository;
import uasz.sn.GestionNotes.Utilisateur.service.EnseignantService;
import uasz.sn.GestionNotes.Utilisateur.service.EtudiantService;

import java.security.Principal;
import java.util.*;

@Controller
public class EnseignantController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private EnseignantService enseignantService;

    @Autowired
    private EnseignantRepository enseignantRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    private InscriptionService inscriptionService;
    @Autowired
    ModuleService moduleService;
    @Autowired
    EtudiantService etudiantService;

    public EnseignantController(EnseignantRepository enseignantRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Afficher la liste des enseignants
    @RequestMapping(value = "/Enseignants", method = RequestMethod.GET)
    public String Enseignant(Model model, Principal principal) {
        List<Enseignant> enseignants = enseignantService.lister();
        model.addAttribute("enseignants", enseignants);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());
        return "Enseignant";
    }

    // Afficher la page d'accueil de l'enseignant
    @RequestMapping(value = "/Enseignant/Accueil", method = RequestMethod.GET)
    public String accueil_Enseignant(Model model, Principal principal) {
        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant != null) {
            model.addAttribute("enseignant", enseignant);
        } else {
            model.addAttribute("error", principal == null ? "Utilisateur non connecté." : "Enseignant non trouvé.");
        }
        return "template-Enseignant";
    }

    // Ajouter un enseignant permanent

    @RequestMapping(value = "/ajouterPermanent", method = RequestMethod.POST)
    public String ajouter_Permanent(Enseignant permanent, RedirectAttributes redirectAttributes) {
        try {
            String password = passwordEncoder.encode("Passer123");
            permanent.setPassword(password);
            permanent.setDateCreation(new Date());
            permanent.setActive(true);

            Role role = utilisateurService.ajouter_Role(new Role("Enseignant"));
            List<Role> roles = new ArrayList<>();
            roles.add(role);
            permanent.setRoles(roles);

            enseignantService.ajouter(permanent);
            redirectAttributes.addFlashAttribute("successMessage", "L'enseignant a été ajouté avec succès !");
            return "redirect:/Enseignants";

        } catch (DataIntegrityViolationException e) {
            // Vérifier si l'erreur est due à un doublon d'email ou username
            if (e.getMessage().contains("UKkq7nt5wyq9v9lpcpgxag2f24a")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Cet email est déjà utilisé. Veuillez en choisir un autre.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'ajout de l'enseignant. Veuillez réessayer.");
            }
            return "redirect:/Enseignants"; // Redirige vers le formulaire avec le message d'erreur
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur inattendue s'est produite. Veuillez réessayer.");
            return "redirect:/Enseignants";
        }
    }



    // Modifier un enseignant permanent
    @RequestMapping(value = "/modifierPermanent", method = RequestMethod.POST)
    public String modifier_Permanent(Enseignant permanant) {
        Enseignant per_modif = enseignantService.rechercher(permanant.getId());
        per_modif.setMatricule(permanant.getMatricule());
        per_modif.setNom(permanant.getNom());
        per_modif.setPrenom(permanant.getPrenom());
        per_modif.setSpecialite(permanant.getSpecialite());
        per_modif.setGrade(permanant.getGrade());

        enseignantService.modifier(per_modif);
        return "redirect:/Enseignants";
    }

    // Activer un enseignant permanent
    @RequestMapping(value = "/activerPermanent", method = RequestMethod.POST)
    public String activer_Permanent(@RequestParam("id") Long id) {
        enseignantService.activer(id);
        return "redirect:/Enseignants";
    }


    // Archiver un enseignant permanent
    @RequestMapping(value = "/archiverPermanent", method = RequestMethod.POST)
    public String archiver_Permanent(Long id) {
        enseignantService.archiver(id);
        return "redirect:/Enseignants";
    }

    // Afficher le formulaire de modification du mot de passe (GET)
    @GetMapping("/EditerPENS")
    public String afficherFormulaire(Model model, Principal principal) {
        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant != null) {
            model.addAttribute("enseignant", enseignant);
            model.addAttribute("message", "Bienvenue sur votre espace enseignant !");
        } else {
            model.addAttribute("error", principal == null ? "Utilisateur non connecté." : "Enseignant non trouvé.");
        }
        return "EditerPENS";
    }

    // Traiter la soumission du formulaire de modification du mot de passe (POST)
    @PostMapping("/EditerPENS")
    public String modifierMotDePasse(
            @RequestParam Long enseignantId,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            // Vérifier que les mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
                return "redirect:/EditerPENS";
            }

            // Vérifier la longueur du mot de passe
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 8 caractères.");
                return "redirect:/EditerPENS";
            }

            // Vérifier si l'enseignant existe
            Optional<Enseignant> optionalEnseignant = enseignantRepository.findById(enseignantId);
            if (optionalEnseignant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Enseignant non trouvé.");
                return "redirect:/EditerPENS";
            }

            // Modifier et sauvegarder le mot de passe (haché)
            Enseignant enseignant = optionalEnseignant.get();
            enseignant.setPassword(passwordEncoder.encode(newPassword));
            enseignantRepository.save(enseignant);

            // Message de succès
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès !");
            return "redirect:/EditerPENS";

        } catch (Exception e) {
            // Gestion des erreurs
            redirectAttributes.addFlashAttribute("error", "Une erreur s'est produite : " + e.getMessage());
            return "redirect:/EditerPENS";
        }
    }

    // Méthode utilitaire pour récupérer l'enseignant connecté
    private Enseignant getEnseignantConnecte(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            return enseignantService.findByUsername(username);
        }
        return null;
    }

    @GetMapping("/etudiantsInscritsModules")
    public String afficherEtudiantsInscritsModules(Model model, Principal principal) {
        // Récupérer l'enseignant connecté
        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant == null) {
            model.addAttribute("error", "Enseignant non trouvé ou non connecté.");
            return "template-Enseignant"; // Retourner à la page d'accueil avec un message d'erreur
        }

        // Récupérer les étudiants inscrits aux modules de l'enseignant
        List<Etudiant> etudiants = enseignantService.getEtudiantsInscritsByEnseignant(enseignant);

        // Ajouter les données au modèle
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("enseignant", enseignant);

        // Retourner la vue qui affiche la liste des étudiants inscrits
        return "etudiantsInscritsModules"; // Nom de la vue Thymeleaf
    }


}