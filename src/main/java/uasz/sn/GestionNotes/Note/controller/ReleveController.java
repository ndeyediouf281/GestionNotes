package uasz.sn.GestionNotes.Note.controller;

import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Note.modele.Notification;
import uasz.sn.GestionNotes.Note.modele.dto.ReleveNotesDTO;
import uasz.sn.GestionNotes.Note.service.DeliberationService;
import uasz.sn.GestionNotes.Note.service.NotificationService;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class ReleveController {
@Autowired
private NotificationService notificationService;
    private final DeliberationService deliberationService;
    private final EtudiantRepository etudiantRepository;
    @Autowired
    private UtilisateurService utilisateurService;

    public ReleveController(DeliberationService deliberationService,
                            EtudiantRepository etudiantRepository) {
        this.deliberationService = deliberationService;
        this.etudiantRepository = etudiantRepository;
    }

    // Affiche la page HTML avec les informations
    @PreAuthorize("hasRole('ETUDIANT')")
    @GetMapping("/releve")
    public String afficherReleve(Authentication authentication, Model model, Principal principal) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Etudiant etudiant = etudiantRepository.findByUsername(userDetails.getUsername());

        if(etudiant == null) {
            throw new IllegalArgumentException("Étudiant non trouvé");
        }

        ReleveNotesDTO releve = deliberationService.genererReleveNotes(etudiant.getId());
        model.addAttribute("releve", releve);
        List<Notification> notifications = notificationService.getNotifications(etudiant);
        model.addAttribute("notifications", notifications != null ? notifications : Collections.emptyList());
        Utilisateur utilisateur = utilisateurService.recherche_Utilisateur(principal.getName());
        model.addAttribute("nom", utilisateur.getNom());
        model.addAttribute("prenom", utilisateur.getPrenom());

        return "releve-form";
    }

    // Gère le téléchargement du PDF
    @PreAuthorize("hasRole('ETUDIANT')")
    @GetMapping("/releve/download")
    public void genererRelevePDF(HttpServletResponse response,
                                 Authentication authentication)
            throws IOException {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Etudiant etudiant = etudiantRepository.findByUsername(userDetails.getUsername());

            ReleveNotesDTO releve = deliberationService.genererReleveNotes(etudiant.getId());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=releve-notes-" + etudiant.getMatricule() + ".pdf");

            deliberationService.genererPDF(releve, response.getOutputStream());
            response.flushBuffer();

        } catch (DocumentException e) {
            throw new IOException("Erreur génération PDF: " + e.getMessage(), e);
        }
    }

    // ... (gestion des exceptions reste inchangée)


    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur système: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Requête invalide: " + ex.getMessage());
    }
    @ResponseBody
    @PostMapping("/notifications/marquer-lue/{id}")
    public ResponseEntity<?> marquerNotificationLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.ok().build();
    }

    private Etudiant getEtudiantConnecte(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return etudiantRepository.findByUsername(userDetails.getUsername());

    }

}