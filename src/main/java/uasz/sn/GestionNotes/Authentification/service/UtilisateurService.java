package uasz.sn.GestionNotes.Authentification.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Authentification.model.Role;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.repository.RoleRepository;
import uasz.sn.GestionNotes.Authentification.repository.UtilisateurRepository;

@Service
@Transactional
public class UtilisateurService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired

    private UtilisateurRepository utilisateurRepository ;
    @Autowired
    private RoleRepository roleRepository;
    public Utilisateur ajouter_Utilisateur(Utilisateur utilisateur){
        utilisateurRepository.save(utilisateur);
        return utilisateur;

    }
    public Role ajouter_Role(Role role){
        roleRepository.save(role);
        return role;
    }

    public Utilisateur recherche_Utilisateur(String username){
        Utilisateur utilisateur = utilisateurRepository.findUtilisateurByusername(username);
        return utilisateur;
    }
    public Utilisateur findByUsername(String username) {
        return utilisateurRepository.findUtilisateurByusername(username);
    }
    public boolean existsByUsername(String username) {
        return utilisateurRepository.existsByUsername(username);
    }

    public void save(Utilisateur utilisateur) {
        utilisateurRepository.save(utilisateur);
    }
    public void saveUtilisateur(Utilisateur utilisateur) {
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword())); // Hash du mot de passe
        utilisateurRepository.save(utilisateur);
    }
    public Role findRoleByName(String roleName) {
        return roleRepository.findRoleByRole(roleName).orElse(null);
    }

    public void saveRole(Role role) {
        roleRepository.save(role);
    }
    // Méthode pour modifier un utilisateur
    public void modifierUtilisateur(Utilisateur utilisateur) {
        // Vérifier si l'utilisateur existe déjà
        if (utilisateurRepository.existsById(utilisateur.getId())) {
            // Sauvegarder les informations mises à jour dans la base de données
            utilisateurRepository.save(utilisateur);
        } else {
            throw new RuntimeException("Utilisateur non trouvé");
        }
    }
}
