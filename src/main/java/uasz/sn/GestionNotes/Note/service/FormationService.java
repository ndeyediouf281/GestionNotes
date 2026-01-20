package uasz.sn.GestionNotes.Note.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Formation;
import uasz.sn.GestionNotes.Note.repository.FormationRepository;

import java.util.List;

@Service
public class FormationService {

        @Autowired
        private FormationRepository formationRepository;

        public List<Formation> listerFormations() {
            return formationRepository.findAll();
        }

        public Formation ajouterFormation(Formation formation) {
            return formationRepository.save(formation);
        }

        public Formation modifierFormation(Formation formation) {
            return formationRepository.save(formation);
        }
        public void supprimerFormation(Formation formation){ formationRepository.delete(formation);}

        public void archiverFormation(Long id) {
            Formation formation = formationRepository.findById(id).get();
            if (formation.isArchive() == true) {
                formation.setArchive(false);
            } else {formation.setArchive(true);}
            formationRepository.save(formation);

        }

        public void activerFormation(Long id) {
            Formation formation = formationRepository.findById(id).orElse(null);
            if (formation.isActive()==true) {
                formation.setActive(false);}
            else {formation.setActive(true);}
            formationRepository.save(formation);
        }

        public boolean existeFormationParNom(String nom) {
            return formationRepository.existsByIntitule(nom);
        }

        public Formation rechercherFormation(Long formationId) {
            return formationRepository.findById(formationId).get();
        }

        public Formation rechercherParNom(String nom) {
            return formationRepository.findByintitule(nom);
        }
    public Formation findFormationById(Long formationId) {
        return formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with id " + formationId));
    }
    }


