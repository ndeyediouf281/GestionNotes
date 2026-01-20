package uasz.sn.GestionNotes.Note.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uasz.sn.GestionNotes.Note.modele.Note;
import uasz.sn.GestionNotes.Note.repository.NoteRepository;


import java.util.List;
@Service
@Transactional
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;
    public List<Note> lister() {
        return noteRepository.findAll();
    }
    // Trouver une note par ID
    public Note trouverParId(Long id) {
        return noteRepository.findById(id).orElse(null);
    }

    // Sauvegarder une note
    public void sauvegarder(Note note) {
        noteRepository.save(note);
    }
}
