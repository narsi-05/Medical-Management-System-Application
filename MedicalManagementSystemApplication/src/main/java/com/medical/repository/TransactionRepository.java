package com.medical.repository;

import com.medical.model.Transaction;
import com.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromUser(User fromUser);
    List<Transaction> findByToUser(User toUser);
    List<Transaction> findByFromUserOrToUser(User fromUser, User toUser);
}
