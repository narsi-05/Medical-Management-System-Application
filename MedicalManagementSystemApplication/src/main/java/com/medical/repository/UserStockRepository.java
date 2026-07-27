package com.medical.repository;

import com.medical.model.Medicine;
import com.medical.model.User;
import com.medical.model.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    List<UserStock> findByUser(User user);
    Optional<UserStock> findByUserAndMedicine(User user, Medicine medicine);
    List<UserStock> findByUserAndQuantityLessThan(User user, int quantity);
}
