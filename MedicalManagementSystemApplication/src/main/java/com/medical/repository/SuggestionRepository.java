package com.medical.repository;

import com.medical.model.Suggestion;
import com.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByToUser(User toUser);
    List<Suggestion> findByFromUser(User fromUser);
    List<Suggestion> findByToUserOrderByCreatedAtDesc(User toUser);
}
