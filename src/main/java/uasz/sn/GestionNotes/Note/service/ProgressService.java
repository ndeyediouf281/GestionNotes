package uasz.sn.GestionNotes.Note.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Note;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Note.modele.dto.StudentProgressDTO;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.repository.NoteRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Note.modele.Formation;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final NoteRepository noteRepository;
    private final ModuleRepository moduleRepository;

    public StudentProgressDTO getStudentProgress(Etudiant etudiant) {
        StudentProgressDTO dto = new StudentProgressDTO();

        try {
            // 1. Calcul de la moyenne générale
            dto.setMoyenneGenerale(calculerMoyenneGenerale(etudiant));

            // 2. Moyennes par module
            dto.setMoyenneParModule(calculerMoyennesParModule(etudiant));

            // 3. Calcul des crédits
            Map<String, Integer> credits = calculerCredits(etudiant);
            dto.setCreditsAcquis(credits.getOrDefault("acquis", 0));
            dto.setCreditsTotaux(credits.getOrDefault("total", 0));
            dto.setProgressionFormation(calculerProgression(dto.getCreditsAcquis(), dto.getCreditsTotaux()));



        } catch (Exception e) {
            log.error("Erreur lors du calcul de la progression pour l'étudiant {}", etudiant.getId(), e);
        }

        // Nouveaux calculs
        Formation formation = etudiant.getFormation();
        if(formation != null) {
            // Moyenne de la promotion
            dto.setMoyennePromotion(
                    noteRepository.calculateMoyennePromotion(Long.valueOf(formation.getId()))
                            .orElse(0.0)
            );

            // Classement
            dto.setRangClassement(
                    determinerRangClassement(etudiant, Long.valueOf(formation.getId()))
            );
        }

        return dto;
    }

    private double calculerMoyenneGenerale(Etudiant etudiant) {
        return Optional.ofNullable(noteRepository.findByEtudiantId(etudiant.getId()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(n -> n.getNote() != null && n.getModule() != null)
                .mapToDouble(Note::getNote)
                .average()
                .orElse(0.0);
    }

    private Map<String, Double> calculerMoyennesParModule(Etudiant etudiant) {
        return noteRepository.findByEtudiantId(etudiant.getId())
                .stream()
                .filter(n -> n.getModule() != null && n.getNote() != null)
                .collect(Collectors.groupingBy(
                        n -> String.format("%s (%s)", n.getModule().getLibelle(), n.getModule().getCode()),
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(Note::getNote),
                                avg -> Math.round(avg * 100.0) / 100.0
                        )
                ));
    }

    private Map<String, Integer> calculerCredits(Etudiant etudiant) {
        // Crédits acquis
        List<Module> modulesReussis = moduleRepository.findModulesReussis(etudiant.getId());
        int creditsAcquis = modulesReussis.stream()
                .mapToInt(Module::getCredit)
                .sum();

        // Crédits totaux fixes à 30
        final int CREDITS_TOTAUX = 30;

        return Map.of(
                "acquis", creditsAcquis,
                "total", CREDITS_TOTAUX
        );
    }

    private double calculerProgression(int acquis, int total) {
        return total > 0 ? (acquis * 100.0) / total : 0.0;
    }

    private String determinerRangClassement(Etudiant etudiant, Long formationId) {
    try {
        List<Object[]> classement = noteRepository.getClassementPromotion(formationId);

        for(int i = 0; i < classement.size(); i++) {
            Long etudiantId = (Long) classement.get(i)[0];
            if(etudiantId.equals(etudiant.getId())) {
                return formaterRang(i + 1, classement.size());
            }
        }
        return "Non classé";

    } catch (Exception e) {
        log.error("Erreur de classement", e);
        return "N/A";
    }
}

private String formaterRang(int position, int total) {
    return switch(position) {
        case 1 -> "🥇 1er/" + total;
        case 2 -> "🥈 2ème/" + total;
        case 3 -> "🥉 3ème/" + total;
        default -> position + "ème/" + total;
    };
}
}
