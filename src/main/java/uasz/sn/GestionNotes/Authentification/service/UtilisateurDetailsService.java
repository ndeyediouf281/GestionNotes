package uasz.sn.GestionNotes.Authentification.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Authentification.model.Utilisateur;
import uasz.sn.GestionNotes.Authentification.repository.UtilisateurRepository;

import java.util.stream.Collectors;

@Service
public class UtilisateurDetailsService implements UserDetailsService {
    private UtilisateurRepository utilisateurRepository;
    public UtilisateurDetailsService(UtilisateurRepository utilisateurRepository){
        this.utilisateurRepository = utilisateurRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Utilisateur utilisateur = utilisateurRepository.findUtilisateurByusername(username);
        if (utilisateur == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé: " + username);
        }

        return User.withUsername(utilisateur.getUsername())
                .password(utilisateur.getPassword())
                .authorities(utilisateur.getRoles().stream()
                        .map(role -> "ROLE_" + role.getRole()) // Ajout de "ROLE_"
                        .collect(Collectors.toList())
                        .toArray(new String[0]) // Convertir la liste en tableau
                )
                .build();
}}
