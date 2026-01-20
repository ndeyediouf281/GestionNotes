package uasz.sn.GestionNotes.Utilisateur.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.stream.Collectors;

@Service
@Transactional

public class EnseignantService {
    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ModuleRepository moduleRepository;

    public Enseignant ajouter(Enseignant enseignant) {
        return enseignantRepository.save(enseignant);
    }

    public List<Enseignant> lister() {
        return enseignantRepository.findAll();
    }

    public Enseignant rechercher(Long id) {
        return enseignantRepository.findById(id).get();
    }

    public Enseignant modifier(Enseignant enseignant) {
        return enseignantRepository.save(enseignant);
    }

    public void supprimer(Long id) {
        enseignantRepository.deleteById(id);
    }

    public void activer(Long id) {
        Enseignant enseignant = enseignantRepository.findById(id).orElse(null);
        if (enseignant.isActive() == true) {
            enseignant.setActive(false);
        } else {
            enseignant.setActive(true);
        }
        enseignantRepository.save(enseignant);
    }


    public void archiver(Long id) {
        Enseignant enseignant = enseignantRepository.findById(id).get();
        if (enseignant.isArchive() == true) {
            enseignant.setArchive(false);
        } else {
            enseignant.setArchive(true);
        }
        enseignantRepository.save(enseignant);

    }

    public Enseignant findById(Long id) {
        return enseignantRepository.findById(id).orElse(null);  // Assurez-vous que l'ID existe dans la base
    }

    public Enseignant findByUsername(String username) {
        return enseignantRepository.findByUsername(username);
    }


    public List<Etudiant> getEtudiantsInscritsByEnseignant(Enseignant enseignant) {
        // Récupérer les modules dont l'enseignant est responsable
        List<Module> modules = moduleRepository.findByEnseignant(enseignant);

        // Récupérer les étudiants inscrits à ces modules
        return modules.stream()
                .flatMap(module -> module.getInscriptions().stream()) // Récupérer les inscriptions
                .map(Inscription::getEtudiant) // Récupérer les étudiants
                .distinct() // Éviter les doublons
                .collect(Collectors.toList());
    }
}








