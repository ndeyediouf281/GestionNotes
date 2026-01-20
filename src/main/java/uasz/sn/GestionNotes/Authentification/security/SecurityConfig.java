package uasz.sn.GestionNotes.Authentification.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity


public class SecurityConfig {
    private static final String[] FOR_ETUDIANT={"/Etudiant/**"};
    private static final String[] FOR_ENSEIGNANT={"/Enseignant/**"};
    private static final String[] FOR_ADMINISTRATEUR={"/Administrateur"};
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/js/**", "/css/**").permitAll()
                                .requestMatchers("/login*", "/logout*", "/register").permitAll()  // Permettre l'accès à /register
                                .requestMatchers("/h2/**").permitAll()
                                .requestMatchers(FOR_ETUDIANT).hasAuthority("ROLE_Etudiant")
                                .requestMatchers(FOR_ENSEIGNANT).hasAuthority("ROLE_Enseignant")
                                .requestMatchers(FOR_ADMINISTRATEUR).hasAuthority("ROLE_Administrateur")
                                .requestMatchers("/assignerResponsable").hasAuthority("ROLE_Administrateur")
                                .requestMatchers(HttpMethod.POST, "/saisirNotes/**").hasRole("Enseignant")
                                .requestMatchers("/template-Enseignant").authenticated()
                                .requestMatchers("/profil").authenticated()

                                .requestMatchers("/api/**", "/apiDTO/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .successHandler((request, response, authentication) -> {
                                    authentication.getAuthorities().forEach(grantedAuthority -> {
                                        try {
                                            String role = grantedAuthority.getAuthority();
                                            if (role.equals("ROLE_Etudiant")) {
                                                response.sendRedirect("/Etudiant/Accueil");
                                            } else if (role.equals("ROLE_Enseignant")) {
                                                response.sendRedirect("/Enseignant/Accueil");
                                            } else if (role.equals("ROLE_Administrateur")) {
                                                response.sendRedirect("/Administrateur/Accueil");
                                            }
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                })
                                .failureUrl("/login?error=true")
                                .permitAll()
                )

                .csrf(csrf ->
                        csrf.ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/**"),
                                new AntPathRequestMatcher("/apiDTO/**")
                        )
                )
                .logout(logout -> logout.permitAll());
        return http.build();
    }

    // Bean pour AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
