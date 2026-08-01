package com.medical.config;

import com.medical.model.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByLicenceNumber("ADMIN001")) {
            User admin = new User();
            admin.setLicenceNumber("ADMIN001");
            admin.setEmail("admin@medical.com");
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setMobileNumber("9999999999");
            admin.setVillage("HQ");
            admin.setMandal("HQ");
            admin.setDistrict("HQ");
            admin.setState("Telangana");
            admin.setRole(User.Role.ADMIN);
            admin.setPassword(passwordEncoder.encode("admin@123"));
            admin.setApprovalStatus(User.ApprovalStatus.APPROVED);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("✅ Default Admin account initialized.");
        }
    }
}
