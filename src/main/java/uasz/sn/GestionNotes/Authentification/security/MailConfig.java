package uasz.sn.GestionNotes.Authentification.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Configurer votre serveur SMTP
        mailSender.setHost("smtp.example.com");
        mailSender.setPort(587); // Par exemple, pour le port SMTP

        // Configuration de l'authentification
        mailSender.setUsername("your-email@example.com");
        mailSender.setPassword("your-email-password");

        // Paramètres supplémentaires pour le mail
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
