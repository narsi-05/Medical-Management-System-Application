package com.medical.service;

import com.medical.model.Suggestion;
import com.medical.model.User;
import com.medical.repository.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    public Suggestion sendSuggestion(User from, User to, String subject, String message) {
        Suggestion s = new Suggestion();
        s.setFromUser(from);
        s.setToUser(to);
        s.setSubject(subject);
        s.setMessage(message);
        return suggestionRepository.save(s);
    }

    public List<Suggestion> getSuggestionsForUser(User user) {
        return suggestionRepository.findByToUserOrderByCreatedAtDesc(user);
    }

    public List<Suggestion> getSentByUser(User user) {
        return suggestionRepository.findByFromUser(user);
    }

    public void markRead(Long id) {
        suggestionRepository.findById(id).ifPresent(s -> {
            s.setReadStatus(true);
            suggestionRepository.save(s);
        });
    }
}
