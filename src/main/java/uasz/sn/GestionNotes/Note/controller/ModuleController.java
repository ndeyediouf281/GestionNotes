package uasz.sn.GestionNotes.Note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.service.InscriptionService;
import uasz.sn.GestionNotes.Note.service.ModuleService;
import uasz.sn.GestionNotes.Note.service.FormationService;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EnseignantRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private FormationService formationService;
    @Autowired
    private  UtilisateurService utilisateurService;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    EnseignantRepository enseignantRepository;
    @Autowired
    private InscriptionService inscriptionService;

    @RequestMapping(value = "/modules", method = RequestMethod.GET)
    public String listerModules(@RequestParam("formationId") Long formationId, Model model, Principal principal) {
        Formation formation = formationService.rechercherFormation(formationId);
        if (formation == null) {
            model.addAttribute("messageErreur", "La formation sélectionnée n'existe pas.");
            return "erreur";
        }

        List<Module> modules = moduleService.listerModulesParFormation(formation);
        model.addAttribute("formation", formation);
        model.addAttribute("modules", modules);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "module"; // Assurez-vous que module.jsp existe
    }

    @RequestMapping(value = "/ajouterModule", method = RequestMethod.POST)
    public String ajouterModule(@ModelAttribute Module module, @RequestParam("formationId") Long formationId, RedirectAttributes redirectAttributes) {
        Formation formation = formationService.rechercherFormation(formationId);
        if (formation == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Formation invalide.");
            return "redirect:/modules?formationId=" + formationId;
        }
        // Vérification si la formation est active
        if (formation.isArchive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Impossible d'ajouter un module à une formation archivée.");
            return "redirect:/modules?formationId=" + formationId;
        }

        // Vérification si la formation est active
        if (!formation.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : La formation sélectionnée est inactive.");
            return "redirect:/modules?formationId=" + formationId;
        }

        if (moduleService.existeParCodeEtLibelle(module.getCode(), module.getLibelle())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même code et libellé existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }
        if (moduleService.existeParCode(module.getCode())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même code  existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }
        if (moduleService.existeParLibelle(module.getLibelle())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même libellé existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }

        module.setFormation(formation);
        moduleService.ajouterModule(module);
        redirectAttributes.addFlashAttribute("successMessage", "Module ajouté avec succès.");
        return "redirect:/modules?formationId=" + formationId;
    }

    @RequestMapping(value = "/modifierModule", method = RequestMethod.POST)
    public String modifierModule(@ModelAttribute Module module, @RequestParam("formationId") Long formationId, RedirectAttributes redirectAttributes) {
        Formation formation = formationService.rechercherFormation(formationId);
        if (formation == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Formation invalide.");
            return "redirect:/modules?formationId=" + formationId;
        }

        if (moduleService.existeParCodeEtLibelle(module.getCode(), module.getLibelle())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même code et libellé existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }
        if (moduleService.existeParCode(module.getCode())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même code  existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }
        if (moduleService.existeParLibelle(module.getLibelle())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Un module avec le même libellé existe déjà.");
            return "redirect:/modules?formationId=" + formationId;
        }
        module.setFormation(formation);
        moduleService.modifierModule(module);
        redirectAttributes.addFlashAttribute("successMessage", "Module modifié avec succès.");
        return "redirect:/modules?formationId=" + formationId;
    }

    @RequestMapping(value = "/supprimerModule", method = RequestMethod.POST)
    public String supprimerModule(@RequestParam("id") Long id,@ModelAttribute Module module, @RequestParam("formationId") Long formationId, RedirectAttributes redirectAttributes) {
        if (moduleService.moduleAssocieAEc(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce module car il est associé à un étudiant.");
            return "redirect:/modules?formationId=" + formationId;
        }

        moduleService.supprimerModule(module);
        redirectAttributes.addFlashAttribute("successMessage", "Module supprimé avec succès.");
        return "redirect:/modules?formationId=" + formationId;
    }


    @RequestMapping(value = "/activerModule", method = RequestMethod.POST)
    public String activerModule(@RequestParam("id") Long id, @RequestParam("formationId") Long formationId) {
        moduleService.activer(id);
        return "redirect:/modules?formationId=" + formationId;
    }

    @RequestMapping(value = "/archiverModule", method = RequestMethod.POST)
    public String archiverModule(@RequestParam("id") Long id, @RequestParam("formationId") Long formationId) {
        moduleService.archiver(id);
        return "redirect:/modules?formationId=" + formationId;
    }
    @GetMapping("/assignerResponsable")
    public String afficherFormulaire(Model model,Principal principal) {
        List<Module> modules = moduleRepository.findAll();
        // Vérification si la formation est active

        List<Enseignant> enseignants = enseignantRepository.findAll();
        model.addAttribute("modules", modules);
        model.addAttribute("enseignants", enseignants);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());
        return "assignerResponsable";  // Nom de la vue
    }

    // Traiter l'affectation du responsable
    @PostMapping("/assignerResponsable")
    public String assignerEnseignantResponsable(@RequestParam Long moduleId, @RequestParam Long enseignantId,RedirectAttributes redirectAttributes) {
        Module module = moduleService.rechercherModule(moduleId);

        if (module.isArchive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : Impossible d'inscrire un etudiant à un module archivée.");
            return "redirect:/liste";
        }

        // Vérification si la formation est active
        if (!module.isActive()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : le module sélectionnée est inactive.");
            return "redirect:/assignerResponsable";
        }moduleService.assignerEnseignantResponsable(moduleId, enseignantId);
        return "redirect:/assignerResponsable";  // Rediriger après l'assignation
    }


}
