package uasz.sn.GestionNotes.Note.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Note;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.repository.NoteRepository;
import uasz.sn.GestionNotes.Note.service.NoteService;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EnseignantRepository;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;

import java.security.Principal;
import java.util.List;

@Controller
public class NoteController {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    NoteService noteService;

    // Afficher le formulaire de saisie des notes
    @GetMapping("/saisirNotes/{etudiantId}")
    public String afficherFormulaireSaisieNotes(
            @PathVariable Long etudiantId,
            Model model,
            Principal principal) {
        // Récupérer l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        // Récupérer l'enseignant connecté
        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant == null) {
            model.addAttribute("error", "Enseignant non trouvé ou non connecté.");
            return "erreur"; // Redirection vers une page d'erreur
        }

        // Récupérer les modules enseignés par cet enseignant
        List<Module> modules = moduleRepository.findByEnseignant(enseignant);


        // Ajouter les objets au modèle
        model.addAttribute("note", new Note());
        model.addAttribute("etudiant", etudiant);
        model.addAttribute("modules", modules);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());


        return "saisirNotes";
    }
    @PostMapping("/saisirNotes/{etudiantId}")
    public String saisirNotes(
            @PathVariable Long etudiantId,
            @ModelAttribute Note note,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant == null) {
            redirectAttributes.addFlashAttribute("error", "Enseignant non trouvé ou non connecté.");
            return "redirect:/Enseignant/accueil";
        }
        // Vérifier si une note existe déjà pour cet étudiant et ce module
        Note existingNote = noteRepository.findByEtudiantAndModule(etudiant, note.getModule());
        if (existingNote != null) {
            // Si une note existe déjà, ajouter un message d'erreur
            redirectAttributes.addFlashAttribute("error", "Une note existe déjà pour cet étudiant dans ce module.");
            return "redirect:/saisirNotes/" + etudiantId;  // Redirection vers le formulaire de saisie
        }

        note.setEtudiant(etudiant);
        note.calculerEtSetMoyenne();
        noteRepository.save(note);

        // Ajout du message de succès
        redirectAttributes.addFlashAttribute("successMessage", "La note a été enregistrée avec succès.");


        return "redirect:/liste-notes"; // Redirection avec le message
    }
    // Afficher les notes saisies sous forme de tableau
    @GetMapping("/liste-notes")
    public String afficherNotesSaisies(Model model, Principal principal) {
        // Récupérer l'enseignant connecté
        Enseignant enseignant = getEnseignantConnecte(principal);
        if (enseignant == null) {
            model.addAttribute("error", "Enseignant non trouvé ou non connecté.");
            return "redirect:/Enseignant/accueil";
        }

        // Récupérer les notes associées aux modules de cet enseignant
        List<Note> notes = noteRepository.findByModuleEnseignant(enseignant.getId());

        // Ajouter les notes au modèle
        model.addAttribute("notes", notes);

        return "liste-notes";
    }


    // Récupérer l'enseignant connecté
    private Enseignant getEnseignantConnecte(Principal principal) {
        if (principal == null) return null;
        String username = principal.getName();
        return enseignantRepository.findByUsername(username);
    }
    @GetMapping("/toutes")
    public String getAllNotes(Model model,Principal principal) {
        List<Note> listeNotes = noteRepository.findAll();
        model.addAttribute("listeNotes", listeNotes);
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());
        return "toutesNotes"; // Retourne la vue contenant la liste des notes
    }
    // Afficher la page de modification d'une note
    @GetMapping("/modifier-note/{id}")
    public String modifierNote(@PathVariable Long id, Model model) {
        // Récupérer la note par ID
        Note note = noteService.trouverParId(id); // Remplacez par la méthode appropriée du service
        if (note != null) {
            model.addAttribute("note", note);
            return "modifierNote";  // Retourne la page de modification
        }
        return "redirect:/liste-notes";  // Redirige si la note n'existe pas
    }

    // Traiter la soumission du formulaire de modification
    @PostMapping("/modifier-note/{id}")
    public String modifierNote(@PathVariable Long id, Note noteModifiee, Model model) {
        // Récupérer la note à modifier
        Note note = noteService.trouverParId(id);
        if (note != null) {
            // Mettre à jour la note avec les nouvelles valeurs
            note.setNoteControleContinu(noteModifiee.getNoteControleContinu());
            note.setNoteExamen(noteModifiee.getNoteExamen());
            note.setMoyenne((noteModifiee.getNoteControleContinu() + noteModifiee.getNoteExamen()) / 2.0);  // Exemple de calcul
            noteService.sauvegarder(note);  // Sauvegarder la note modifiée dans la base de données
            model.addAttribute("successMessage", "Note modifiée avec succès !");
            return "redirect:/liste-notes";  // Redirige vers la liste des notes
        }
        return "redirect:/liste-notes";  // Redirige si la note n'existe pas
    }

}
