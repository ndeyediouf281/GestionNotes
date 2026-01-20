package uasz.sn.GestionNotes.Note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.repository.FormationRepository;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.service.FormationService;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class FormationControlleur {

    @Autowired
    private FormationService formationService;
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private FormationRepository formationRepository;
    @Autowired
    private ModuleRepository moduleRepository;

    @RequestMapping(value = "/formations", method = RequestMethod.GET)
    public String listerFormations(Model model, Principal principal) {
        List<Formation> formations = formationService.listerFormations();
        model.addAttribute("formations", formations);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "formation";
    }

    @RequestMapping(value = "/ajouterFormation", method = RequestMethod.POST)
    public String ajouterFormation(@ModelAttribute Formation formation, RedirectAttributes redirectAttributes) {
        if (formationService.existeFormationParNom(formation.getIntitule())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une formation avec le même nom  existe déjà.");
            return "redirect:/formations";
        }
        formationService.ajouterFormation(formation);
        return "redirect:/formations";
    }

    @RequestMapping(value = "/modifierFormation", method = RequestMethod.POST)
    public String modifierFormation(@ModelAttribute Formation formation, RedirectAttributes redirectAttributes) {
        if (formationService.existeFormationParNom(formation.getIntitule())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une formation avec le même nom  existe déjà.");
            return "redirect:/formations";
        }
        formationService.modifierFormation(formation);
        return "redirect:/formations";
    }

    @RequestMapping(value = "/archiverFormation", method = RequestMethod.POST)
    public String archiverFormation(@RequestParam("id") Long id) {
        formationService.archiverFormation(id);
        return "redirect:/formations";
    }

    @RequestMapping(value = "/activerFormation", method = RequestMethod.POST)
    public String activerFormation(@RequestParam("id") Long id) {
        formationService.activerFormation(id);
        return "redirect:/formations";
    }

    @RequestMapping(value = "/rechercherFormation", method = RequestMethod.GET)
    public String rechercherFormation(@RequestParam("nom") String nom, Model model) {
        Formation formation = formationService.rechercherParNom(nom);
        model.addAttribute("formation", formation);
        return "formation_details";
    }
    @GetMapping("/formation/enseignants")
    public String getEnseignantsByFormation(@RequestParam(required = false) Long formationId, Model model,Principal principal) {
        // Récupérer et ajouter la liste des formations au modèle pour toujours l'afficher dans la vue
        List<Formation> formations = formationService.listerFormations();
        model.addAttribute("formations", formations);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        // Si aucun ID de formation n'est fourni, retourner simplement la vue avec la liste déroulante
        if (formationId == null) {
            model.addAttribute("message", "Veuillez sélectionner une formation.");
            return "ListeEnseignantsFormation";
        }

        // Récupérer la formation sélectionnée
        Formation formation = formationRepository.findById(formationId).orElse(null);
        if (formation != null) {
            List<Module> modules = moduleRepository.findByFormation(formation);
            Set<Enseignant> enseignants = modules.stream()
                    .map(Module::getEnseignant)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            model.addAttribute("formation", formation);
            model.addAttribute("enseignants", enseignants);
        }

        return "ListeEnseignantsFormation";
    }

}


