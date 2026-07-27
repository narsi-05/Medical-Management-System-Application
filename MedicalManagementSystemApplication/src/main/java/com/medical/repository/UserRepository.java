package com.medical.repository;

import com.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLicenceNumber(String licenceNumber);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndApprovalStatus(User.Role role, User.ApprovalStatus status);
    boolean existsByLicenceNumber(String licenceNumber);
    boolean existsByEmail(String email);
}
