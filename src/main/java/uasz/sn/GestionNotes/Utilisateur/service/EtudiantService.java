package uasz.sn.GestionNotes.Utilisateur.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Inscription;
import uasz.sn.GestionNotes.Note.repository.ModuleRepository;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.model.Etudiant;
import uasz.sn.GestionNotes.Note.modele.Module;
import uasz.sn.GestionNotes.Utilisateur.repository.EtudiantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EtudiantService {

    @Autowired
    private EtudiantRepository etudiantRepository;
    @Autowired
    ModuleRepository moduleRepository;

    public void ajouterEtudiant(Etudiant etudiant) {
        etudiantRepository.save(etudiant);
    }

    public List<Etudiant> listerEtudiant() {
        return etudiantRepository.findAll(); // Doit retourner tous les étudiants
    }

    public Etudiant rechercher(Long id) {
        return etudiantRepository.findById(id).orElse(null);
    }

    public void modifierEtudiant(Etudiant etudiant) {
        etudiantRepository.save(etudiant);
    }



    public Etudiant findByUsername(String username) {
        return etudiantRepository.findByUsername(username);
    }
    public void activer(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id).get();
        if (etudiant.isActive() == true) {
            etudiant.setActive(false);
        } else {
            etudiant.setActive(true);
        }
        etudiantRepository.save(etudiant);
    }

    public void archiver(Long id) {
        Etudiant etudiant = etudiantRepository.findById(id).get();
        if (etudiant.isArchive() == true) {
            etudiant.setArchive(false);
        } else {
            etudiant.setArchive(true);
        }
        etudiantRepository.save(etudiant);
    }
    public List<Module> getModulesByEtudiant(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        // Accéder aux modules via les inscriptions
        List<Module> modules = etudiant.getInscriptions().stream()
                .map(Inscription::getModule)
                .collect(Collectors.toList());

        return modules;

    }
    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }
    public void updateEtudiant(Etudiant etudiant) {
        etudiantRepository.save(etudiant);
    }
}













