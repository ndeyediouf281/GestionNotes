package uasz.sn.GestionNotes.Note.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Utilisateur.repository.EnseignantRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    EnseignantRepository enseignantRepository;

    public boolean existeParCodeEtLibelle(String code, String libelle) {
        return moduleRepository.existsByCodeAndLibelle(code, libelle);
    }

    public boolean existeParCode(String code) {
        return moduleRepository.existsByCode(code);
    }

    public boolean existeParLibelle(String libelle) {
        return moduleRepository.existsByLibelle(libelle);
    }

    public void ajouterModule(Module module) {
        moduleRepository.save(module);
    }
    public List<Module> listerModulesParFormation(Formation formation) {
        return moduleRepository.findByFormation(formation);
    }


    public List<Module> listerModule() {
        return moduleRepository.findAll();
    }

    public Module rechercherModule(Long moduleId) {
        Optional<Module> module = moduleRepository.findById(moduleId);
        return module.orElse(null); // Évite l'erreur si le moduleId n'existe pas
    }

    public void modifierModule(Module module) {
        moduleRepository.save(module);
    }

    public void supprimerModule(Module module) {
        moduleRepository.delete(module);
    }
    public boolean moduleAssocieAEc(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.isPresent() && (module.get().getFormation() != null || module.get().getEnseignant() != null);
    }


    public void activer(Long id) {
        Optional<Module> optionalModule = moduleRepository.findById(id);
        if (optionalModule.isPresent()) {
            Module module = optionalModule.get();
            module.setActive(!module.isActive()); // Inverse l'état actif
            moduleRepository.save(module);
        }
    }

    public void archiver(Long id) {
        Optional<Module> optionalModule = moduleRepository.findById(id);
        if (optionalModule.isPresent()) {
            Module module = optionalModule.get();
            module.setArchive(!module.isArchive()); // Inverse l'état archive
            moduleRepository.save(module);
        }
    }
    public Module assignerEnseignantResponsable(Long moduleId, Long enseignantId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

        module.setEnseignant(enseignant); // Affectation de l'enseignant
        return moduleRepository.save(module);
    }
    public Module rechercher(Long moduleId) {
        return moduleRepository.findById(moduleId).orElse(null); // Retourne null si le module n'est pas trouvé
    }

    public Module getModuleById(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));
    }


}
