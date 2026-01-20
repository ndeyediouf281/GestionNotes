package uasz.sn.GestionNotes.Note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uasz.sn.GestionNotes.Note.modele.Deliberation;

public interface DeliberationRepository extends JpaRepository<Deliberation,Long> {
}
