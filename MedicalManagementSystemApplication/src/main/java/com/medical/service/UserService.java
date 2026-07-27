package com.medical.service;

import com.medical.model.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setApprovalStatus(User.ApprovalStatus.PENDING);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    public void setPassword(String licenceNumber, String rawPassword) {
        User user = userRepository.findByLicenceNumber(licenceNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    public boolean existsByLicenceNumber(String licenceNumber) {
        return userRepository.existsByLicenceNumber(licenceNumber);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByLicenceNumber(String licenceNumber) {
        return userRepository.findByLicenceNumber(licenceNumber);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getPendingDealers() {
        return userRepository.findByRoleAndApprovalStatus(User.Role.DEALER, User.ApprovalStatus.PENDING);
    }

    public List<User> getPendingUsersForDealer() {
        List<User> pending = userRepository.findByRoleAndApprovalStatus(User.Role.HOSPITAL, User.ApprovalStatus.PENDING);
        pending.addAll(userRepository.findByRoleAndApprovalStatus(User.Role.MEDICAL_SHOP, User.ApprovalStatus.PENDING));
        return pending;
    }

    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        user.setEnabled(false);
        userRepository.save(user);
    }

    public List<User> getAllDealers() {
        return userRepository.findByRole(User.Role.DEALER);
    }

    public List<User> getAllApprovedDealers() {
        return userRepository.findByRoleAndApprovalStatus(User.Role.DEALER, User.ApprovalStatus.APPROVED);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
