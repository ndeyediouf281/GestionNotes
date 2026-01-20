package uasz.sn.GestionNotes.Note.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Note.repository.FormationRepository;
import uasz.sn.GestionNotes.Note.repository.InscriptionRepository;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;
import uasz.sn.GestionNotes.Utilisateur.service.EtudiantService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final FormationRepository formationRepository;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    EtudiantService etudiantService;
    @Autowired
    ModuleService moduleService;
    @Autowired
    FormationService formationService;

    public InscriptionService(InscriptionRepository inscriptionRepository, EtudiantRepository etudiantRepository, FormationRepository formationRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.etudiantRepository = etudiantRepository;
        this.formationRepository = formationRepository;
    }

    @Transactional
    public String inscrireEtudiant(Long etudiantId, Long formationId) {
        Optional<Etudiant> etudiantOpt = etudiantRepository.findById(etudiantId);
        Optional<Formation> formationOpt = formationRepository.findById(formationId);

        if (etudiantOpt.isPresent() && formationOpt.isPresent()) {
            Inscription inscription = new Inscription();
            inscription.setEtudiant(etudiantOpt.get());
            inscription.setFormation(formationOpt.get());
            inscription.setDateInscription(LocalDate.now());
            inscription.setActive(true);
            inscriptionRepository.save(inscription);
            return "L'étudiant a été inscrit avec succès !";
        }
        return "Erreur : Étudiant ou Formation introuvable.";
    }

    @Transactional
    public String desinscrireEtudiant(Long etudiantId, Long formationId) {
        Optional<Inscription> inscriptionOpt = inscriptionRepository.findByEtudiantIdAndFormationId(etudiantId, formationId);
        if (inscriptionOpt.isPresent()) {
            Inscription inscription = inscriptionOpt.get();
            inscription.setActive(false);
            inscriptionRepository.save(inscription);
            return "L'étudiant a été désinscrit avec succès !";
        }
        return "Erreur : Inscription introuvable.";
    }

    public List<Inscription> getAllInscriptions() {
        return inscriptionRepository.findAll();
    }



/*
    @Transactional
    public void inscrireEtudiantAuModule(Long etudiantId, Long moduleId, Long formationId) {
        // Récupérer l'étudiant, le module et la formation
        Etudiant etudiant = etudiantService.rechercher(etudiantId);
        Module module = moduleService.rechercher(moduleId);
        Formation formation = formationService.rechercherFormation(formationId);

        // Vérifier que les objets existent
        if (etudiant == null) {
            throw new RuntimeException("Étudiant non trouvé avec l'ID : " + etudiantId);
        }
        if (module == null) {
            throw new RuntimeException("Module non trouvé avec l'ID : " + moduleId);
        }
        if (formation == null) {
            throw new RuntimeException("Formation non trouvée avec l'ID : " + formationId);
        }

        // Vérifier si l'inscription existe déjà
        Optional<Inscription> inscriptionExistante = inscriptionRepository.findByEtudiantIdAndModuleIdAndFormationId(etudiantId, moduleId, formationId);
        if (inscriptionExistante.isPresent()) {
            throw new RuntimeException("L'étudiant est déjà inscrit à ce module dans cette formation.");
        }

        // Créer une nouvelle inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setModule(module);
        inscription.setFormation(formation);

        // Sauvegarder l'inscription
        inscriptionRepository.save(inscription);
    }

 */
public void inscrireEtudiantModule(Long etudiantId, Long moduleId) {
    Etudiant etudiant = etudiantRepository.findById(etudiantId).orElseThrow();
    Module module = moduleRepository.findById(moduleId).orElseThrow();

    // Récupérer la formation liée au module
    Formation formation = module.getFormation();

    Inscription inscription = new Inscription();
    inscription.setEtudiant(etudiant);
    inscription.setModule(module);
    inscription.setFormation(formation); // Assigner la formation
    inscription.setDateInscription(LocalDate.now());
    inscription.setActive(true);

    inscriptionRepository.save(inscription);
}
    // Méthode pour désinscrire un étudiant d'un module
    public void desinscrireEtudiantModule(Long etudiantId, Long moduleId) {
        // Trouver l'inscription correspondante
        Inscription inscription = inscriptionRepository
                .findByEtudiantIdAndModuleId(etudiantId, moduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune inscription trouvée pour cet étudiant et ce module."
                ));

        // Supprimer l'inscription
        inscriptionRepository.delete(inscription);
    }

    public boolean existeInscription(Long etudiantId, Long moduleId) {
        return inscriptionRepository.existsByEtudiantIdAndModuleId(etudiantId, moduleId);
    }
    // Récupérer les inscriptions aux FORMATIONS uniquement
    public List<Inscription> getInscriptionsFormations() {
        return inscriptionRepository.findByFormationIsNotNullAndModuleIsNull();
    }

    // Récupérer les inscriptions aux MODULES uniquement
    public List<Inscription> getInscriptionsModules() {
        return inscriptionRepository.findByModuleIsNotNull();
    }

/*
    @Transactional
    public void desinscrireEtudiantDuModule(Long etudiantId, Long moduleId, Long formationId) {
        // Récupérer l'inscription à supprimer
        Optional<Inscription> inscription = inscriptionRepository.findByEtudiantIdAndModuleIdAndFormationId(etudiantId, moduleId, formationId);
        if (inscription.isPresent()) {
            inscriptionRepository.delete(inscription.get());
        } else {
            throw new RuntimeException("Inscription non trouvée.");
        }
    }

 */
    public List<Etudiant> getEtudiantsInscritsByModuleId(Long moduleId) {
        // Vérifiez si la méthode du repository fonctionne bien
        List<Inscription> inscriptions = inscriptionRepository.findByModuleId(moduleId);

        return inscriptions.stream()
                .map(Inscription::getEtudiant)
                .collect(Collectors.toList());
    }

    public List<Etudiant> getEtudiantsInscritsByFormationId(Long formationId) {
        // Récupérer les inscriptions pour la formation donnée
        return inscriptionRepository.findByFormationId(formationId);

    }
    // Méthode pour récupérer les modules auxquels un étudiant est inscrit
    public List<Module> getModulesByEtudiant(Long etudiantId) {
        // Récupérer l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        // Récupérer toutes les inscriptions de cet étudiant
        List<Inscription> inscriptions = inscriptionRepository.findByEtudiant(etudiant);

        // Extraire la liste des modules à partir des inscriptions
        return inscriptions.stream()
                .map(Inscription::getModule)
                .collect(Collectors.toList());
    }
    public boolean estDejaInscrit(Long etudiantId, Long formationId) {
        return inscriptionRepository.existsByEtudiantIdAndFormationId(etudiantId, formationId);
    }

}


