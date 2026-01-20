package uasz.sn.GestionNotes.Note.service;



import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Note;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.modele.dto.DeliberationDTO;
import uasz.sn.GestionNotes.Note.modele.dto.NoteComponentDTO;
import uasz.sn.GestionNotes.Note.modele.dto.NoteModuleDTO;
import uasz.sn.GestionNotes.Note.modele.dto.ReleveNotesDTO;
import uasz.sn.GestionNotes.Note.repository.NoteRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;



import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DeliberationService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private InscriptionService inscriptionService;

    public List<DeliberationDTO> deliberer(Long formationId) {
        List<Etudiant> etudiants = inscriptionService.getEtudiantsInscritsByFormationId(formationId);
        List<DeliberationDTO> deliberations = new ArrayList<>();

        for (Etudiant etudiant : etudiants) {
            List<Note> notes = noteRepository.findByEtudiantId(etudiant.getId());

            // Calcul des crédits et de la moyenne
            Map<Module, List<Note>> notesParModule = notes.stream()
                    .collect(Collectors.groupingBy(Note::getModule));

            int totalCredits = 0;
            double sommeMoyennes = 0.0;
            int nombreNotes = 0;

            for (Map.Entry<Module, List<Note>> entry : notesParModule.entrySet()) {
                Module module = entry.getKey();
                List<Note> moduleNotes = entry.getValue();

                // Calcul moyenne du module
                double moyenneModule = moduleNotes.stream()
                        .mapToDouble(n -> n.getMoyenne() != null ? n.getMoyenne() : 0.0)
                        .average()
                        .orElse(0.0);

                // Ajout des crédits si module validé
                if (moyenneModule >= 10.0) {
                    totalCredits += module.getCredit();
                }

                // Calcul pour moyenne générale
                sommeMoyennes += moyenneModule;
                nombreNotes++;
            }

            // Calcul final
            totalCredits = Math.min(totalCredits, 30);
            boolean isAdmis = totalCredits == 30;
            Double moyenneGenerale = nombreNotes > 0 ? sommeMoyennes / nombreNotes : 0.0;

            // Création DTO
            DeliberationDTO dto = new DeliberationDTO();
            dto.setEtudiant(etudiant);
            dto.setMoyenne(moyenneGenerale);
            dto.setMention(calculerMention(moyenneGenerale));
            dto.setTotalCredits(totalCredits);
            dto.setAdmis(isAdmis);

            deliberations.add(dto);
        }

        return deliberations;
    }

    public Map<String, Object> calculerStatistiques(List<DeliberationDTO> deliberations) {
        Map<String, Object> stats = new HashMap<>();

        int totalEtudiants = deliberations.size();
        long admis = deliberations.stream().filter(DeliberationDTO::isAdmis).count();
        double tauxReussite = totalEtudiants > 0 ? (admis * 100.0) / totalEtudiants : 0.0;

        Map<String, Long> mentions = deliberations.stream()
                .filter(d -> d.getMention() != null && !d.getMention().isEmpty())
                .collect(Collectors.groupingBy(
                        DeliberationDTO::getMention,
                        Collectors.counting()
                ));

        stats.put("totalEtudiants", totalEtudiants);
        stats.put("admis", admis);
        stats.put("tauxReussite", tauxReussite);
        stats.put("mentions", mentions);

        return stats;
    }

    public ReleveNotesDTO genererReleveNotes(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new EntityNotFoundException("Étudiant non trouvé"));

        List<Note> notes = noteRepository.findByEtudiantId(etudiantId);

        Map<Module, List<Note>> notesParModule = notes.stream()
                .collect(Collectors.groupingBy(Note::getModule));

        List<NoteModuleDTO> modules = notesParModule.entrySet().stream()
                .map(entry -> convertirEnModuleDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Calcul des crédits (uniquement les modules validés)
        int totalCredits = modules.stream()
                .filter(module -> module.getMoyenneModule() >= 10.0)
                .mapToInt(NoteModuleDTO::getCredits)
                .sum();

        // Application du plafond à 30 crédits
        totalCredits = Math.min(totalCredits, 30);

        // Calcul admission
        boolean isAdmis = totalCredits == 30;

        // Calcul moyenne générale pondérée
        double totalPondere = modules.stream()
                .mapToDouble(m -> m.getMoyenneModule() * m.getCoefficient())
                .sum();

        int totalCoefficients = modules.stream()
                .mapToInt(NoteModuleDTO::getCoefficient)
                .sum();

        double moyenneGenerale = totalCoefficients > 0 ?
                totalPondere / totalCoefficients : 0.0;

        return new ReleveNotesDTO(
                etudiant,
                modules,
                moyenneGenerale,
                totalCredits,
                calculerMention(moyenneGenerale),
                isAdmis // Admission basée sur les crédits
        );
    }

    private NoteModuleDTO convertirEnModuleDTO(Module module, List<Note> notes) {
        List<NoteComponentDTO> components = notes.stream()
                .map(this::convertirEnComponentDTO)
                .collect(Collectors.toList());

        // Calcul moyenne module
        double moyenneModule = components.stream()
                .mapToDouble(NoteComponentDTO::getMoyenneEC)
                .average()
                .orElse(0.0);

        return new NoteModuleDTO(
                module.getCode(),
                module.getLibelle(),
                module.getCredit(),
                module.getCoefficient(),
                components,
                moyenneModule,
                calculerMention(moyenneModule)
        );
    }

    private NoteComponentDTO convertirEnComponentDTO(Note note) {
        // Utilisation du libellé du module comme nom de composant
        String libelleComponent = note.getModule().getLibelle();

        return new NoteComponentDTO(
                libelleComponent,
                note.getNoteControleContinu(),
                note.getNoteExamen(),
                note.getMoyenne()
        );
    }

    private String calculerMention(double moyenne) {
        if (moyenne >= 16) return "Très Bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez Bien";
        if (moyenne >= 10) return "Admis";
        return "pas de mention";
    }

    public void genererPDF(ReleveNotesDTO releve, OutputStream outputStream)
            throws DocumentException, IOException {

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        try {
            document.open();
            ajouterEnTete(document, releve);
            ajouterTableauPrincipal(document, releve);
            ajouterConclusion(document, releve);
        } finally {
            document.close();
        }
    }

    private void ajouterEnTete(Document document, ReleveNotesDTO releve)
            throws DocumentException {

        // Titre principal
        Font fontTitre = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph titre = new Paragraph("RELEVÉ DE NOTES OFFICIEL", fontTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        document.add(Chunk.NEWLINE);

        // Informations étudiant
        Font fontInfo = new Font(Font.FontFamily.HELVETICA, 12);
        Etudiant etudiant = releve.getEtudiant();
        document.add(new Paragraph("Étudiant: " + etudiant.getPrenom() + " " + etudiant.getNom(), fontInfo));
        document.add(new Paragraph("Formation: " + etudiant.getFormation().getIntitule(), fontInfo));
        document.add(new Paragraph("Session: 1ère Session - Année 2023/2024", fontInfo));

        // Séparateur
        document.add(new Chunk(new LineSeparator()));
    }

    private void ajouterTableauPrincipal(Document document, ReleveNotesDTO releve)
            throws DocumentException {

        PdfPTable mainTable = new PdfPTable(4);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{4f, 2f, 2f, 2f});

        // En-têtes du tableau
        ajouterCelluleHeader(mainTable, "Unités d'Enseignement");
        ajouterCelluleHeader(mainTable, "CC");
        ajouterCelluleHeader(mainTable, "Examen");
        ajouterCelluleHeader(mainTable, "Moyenne");

        for (NoteModuleDTO module : releve.getModules()) {
            // En-tête du module
            PdfPCell moduleCell = new PdfPCell(new Phrase(
                    module.getCodeModule() + " - " + module.getLibelleModule() +
                            " (Coeff: " + module.getCoefficient() + ")",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)
            ));
            moduleCell.setBackgroundColor(new BaseColor(51, 51, 51));
            moduleCell.setColspan(4);
            moduleCell.setPadding(8);
            mainTable.addCell(moduleCell);

            // Composants du module
            for (NoteComponentDTO composant : module.getComponents()) {
                ajouterCellule(mainTable, composant.getLibelleComponent());
                ajouterCellule(mainTable, formatNote(composant.getNoteCC()));
                ajouterCellule(mainTable, formatNote(composant.getNoteExamen()));
                ajouterCellule(mainTable, formatNote(composant.getMoyenneEC()));
            }

            // Résumé module
            PdfPCell resumeCell = new PdfPCell(new Phrase(
                    "Moyenne UE: " + formatNote(module.getMoyenneModule()) +
                            " | Crédits: " + module.getCredits() +
                            " | Mention: " + module.getMentionModule(),
                    new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)
            ));
            resumeCell.setColspan(4);
            resumeCell.setBorder(Rectangle.NO_BORDER);
            mainTable.addCell(resumeCell);
        }

        document.add(mainTable);
    }

    private void ajouterConclusion(Document document, ReleveNotesDTO releve)
            throws DocumentException {

        // Résumé global
        Font fontResume = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        document.add(new Paragraph("Moyenne Générale: " + formatNote(releve.getMoyenneGenerale()), fontResume));
        document.add(new Paragraph("Crédits Totaux Acquits: " + releve.getTotalCredits(), fontResume));
        document.add(new Paragraph("Mention: " + releve.getMentionGenerale(), fontResume));
        // Décision d'admission
        String decision = releve.isAdmis() ? "ADMIS" : "NON ADMIS";
        Font fontDecision = new Font(Font.FontFamily.HELVETICA, 14,
                Font.BOLD, releve.isAdmis() ? BaseColor.GREEN : BaseColor.RED);

        Paragraph decisionParagraph = new Paragraph("Décision: " + decision, fontDecision);
        decisionParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(decisionParagraph);

        // Détails crédits
        Paragraph creditsParagraph = new Paragraph();
        creditsParagraph.add("Crédits obtenus: " + releve.getTotalCredits() + "/30");
        creditsParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(creditsParagraph);


        // Signature
        Paragraph signature = new Paragraph();
        signature.add(new Chunk("Le Directeur des Études\n",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        signature.add(new Chunk("_________________________"));
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
    }

    // Méthodes utilitaires
    private String formatNote(Double note) {
        return note != null ?
                String.format("%.2f", note).replace(".", ",") :
                "-";
    }

    private void ajouterCelluleHeader(PdfPTable table, String texte) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(new BaseColor(51, 51, 51));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void ajouterCellule(PdfPTable table, String contenu) {
        PdfPCell cell = new PdfPCell(new Phrase(contenu));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}



