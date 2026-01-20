package uasz.sn.GestionNotes.Utilisateur.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.repository.UtilisateurRepository;
import uasz.sn.GestionNotes.Authentification.service.UtilisateurService;
import uasz.sn.GestionNotes.Utilisateur.model.Administrateur;
import uasz.sn.GestionNotes.Utilisateur.model.Enseignant;
import uasz.sn.GestionNotes.Utilisateur.repository.AdministrateurRepository;

@Service
@Transactional

public class AdministrateurService {
    @Autowired
    private AdministrateurRepository administrateurRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Administrateur  ajouter(Administrateur administrateur){
        return administrateurRepository.save(administrateur);
    }
    public Administrateur findByUsername(String username) {
        return administrateurRepository.findByUsername(username);
    }
    public Utilisateur recherche_Utilisateur(String username){
        Utilisateur utilisateur = utilisateurRepository.findUtilisateurByusername(username);
        return utilisateur;
}
}
