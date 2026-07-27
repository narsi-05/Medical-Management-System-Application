package com.medical.config;

import com.medical.model.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String licenceNumber) throws UsernameNotFoundException {
        User user = userRepository.findByLicenceNumber(licenceNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + licenceNumber));
        return new CustomUserDetails(user);
    }
}
