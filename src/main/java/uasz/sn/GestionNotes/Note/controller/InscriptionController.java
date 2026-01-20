package uasz.sn.GestionNotes.Note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.repository.FormationRepository;
import uasz.sn.GestionNotes.Note.service.InscriptionService;
import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.service.ModuleService;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Note.service.FormationService;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;
import uasz.sn.GestionNotes.Utilisateur.service.EtudiantService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;

    @Autowired
    private FormationService formationService;

    @Autowired
    private EtudiantService etudiantService;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private FormationRepository formationRepository;

    @Autowired
    private ModuleService moduleService;
    @Autowired
    private UtilisateurService utilisateurService;

    @RequestMapping(value = "/inscrire", method = RequestMethod.POST)
    public String inscrireEtudiant(@RequestParam("etudiantId") Long etudiantId,
                                   @RequestParam("formationId") Long formationId,
                                   RedirectAttributes redirectAttributes) {
        Etudiant etudiant = etudiantService.rechercher(etudiantId);
        Formation formation = formationService.rechercherFormation(formationId);

        if (etudiant == null || formation == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Étudiant ou formation invalide.");
            return "redirect:/liste";
        }
        // Vérification si la formation est active
        if (formation.isArchive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Impossible d'inscrire un etudiant à une formation archivée.");
            return "redirect:/liste";
        }

        // Vérification si la formation est active
        if (!formation.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : La formation sélectionnée est inactive.");
            return "redirect:/liste";
        }
        // Vérifier si l'étudiant est déjà inscrit à cette formation
        if (inscriptionService.estDejaInscrit(etudiantId, formationId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "L'étudiant est déjà inscrit à cette formation.");
            return "redirect:/liste";
        }

        String message = inscriptionService.inscrireEtudiant(etudiantId, formationId);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/liste"; // Redirection vers la liste des inscriptions
    }

    @RequestMapping(value = "/desinscrire", method = RequestMethod.POST)
    public String desinscrireEtudiant(@RequestParam("etudiantId") Long etudiantId,
                                      @RequestParam("formationId") Long formationId,
                                      RedirectAttributes redirectAttributes) {
        Etudiant etudiant = etudiantService.rechercher(etudiantId);
        Formation formation = formationService.rechercherFormation(formationId);

        if (etudiant == null || formation == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Étudiant ou formation invalide.");
            return "redirect:/inscription/liste";
        }

        String message = inscriptionService.desinscrireEtudiant(etudiantId, formationId);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/liste"; // Redirection après désinscription
    }

    @RequestMapping(value = "/liste", method = RequestMethod.GET)
    public String afficherListeInscriptions(Model model, Principal principal) {
        model.addAttribute("inscriptions", inscriptionService.getInscriptionsFormations());
        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Formation> formations = formationRepository.findAll();
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("formations", formations);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "liste"; // Retourne la vue `liste.html` dans `templates/inscription`
    }

    @GetMapping("/inscriptionModule")
    public String afficherPageInscriptionModule(Model model,Principal principal) {
        model.addAttribute("etudiants", etudiantService.listerEtudiant());
        model.addAttribute("modules", moduleService.listerModule());
        model.addAttribute("formations", formationService.listerFormations());
        model.addAttribute("inscriptions", inscriptionService.getInscriptionsModules());
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "inscriptionModule";
    }
    @PostMapping("/inscrireModule")
    public String inscrireEtudiantModule(
            @RequestParam Long etudiantId,
            @RequestParam Long moduleId,
            RedirectAttributes redirectAttributes) {
        Module module = moduleService.rechercherModule(moduleId);

        // Vérification si la formation est active
        if (module.isArchive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Impossible d'inscrire un etudiant à un module archivée.");
            return "redirect:/liste";
        }
        // Vérification si la formation est active
        if (!module.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : le module sélectionnée est inactive.");
            return "redirect:/liste";
        }

        try {
            if (inscriptionService.existeInscription(etudiantId, moduleId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cet étudiant est déjà inscrit à ce module.");
                return "redirect:/inscriptionModule";
            }

            inscriptionService.inscrireEtudiantModule(etudiantId, moduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie !");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur technique : " + e.getMessage());
        }

        return "redirect:/inscriptionModule";
    }

    // Désinscription
    @PostMapping("/desinscrireModule")
    public String desinscrireEtudiantModule(
            @RequestParam Long etudiantId,
            @RequestParam Long moduleId,
            RedirectAttributes redirectAttributes) {

        try {
            inscriptionService.desinscrireEtudiantModule(etudiantId, moduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Désinscription réussie !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        return "redirect:/inscriptionModule";
    }
    @GetMapping("/etudiantsInscrits")
    public String afficherEtudiantsInscrits(@RequestParam(name = "moduleId", required = false) Long moduleId, Model model, Principal principal) {
        // Charger tous les modules
        List<Module> modules = moduleService.listerModule();
        model.addAttribute("modules", modules);

        // Vérifier si un module a été sélectionné
        List<Etudiant> etudiants = null;
        if (moduleId != null) {
            etudiants = inscriptionService.getEtudiantsInscritsByModuleId(moduleId);
        }
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("selectedModuleId", moduleId);

        // Ajouter les informations de l'utilisateur connecté
        var utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "etudiantsInscrits"; // Nom de la vue Thymeleaf
    }

    @GetMapping("/etudiantsInscritsFormation")
    public String afficherEtudiantsInscritsFormation(@RequestParam(name = "formationId", required = false) Long formationId, Model model, Principal principal) {
        // Récupérer toutes les formations
        List<Formation> formations = formationService.listerFormations();
        model.addAttribute("formations", formations);

        // Récupérer les étudiants inscrits si une formation est sélectionnée
        List<Etudiant> etudiants = null;
        if (formationId != null) {
            etudiants = inscriptionService.getEtudiantsInscritsByFormationId(formationId);
        }
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("selectedFormationId", formationId);

        // Ajouter les informations de l'utilisateur connecté
        var utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "etudiantsInscritsFormation"; // Vue Thymeleaf
    }


}