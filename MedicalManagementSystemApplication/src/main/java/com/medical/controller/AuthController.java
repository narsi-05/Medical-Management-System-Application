package com.medical.controller;

import com.medical.model.User;
import com.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid Licence Number or Password.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") User user,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) return "register";

        if (userService.existsByLicenceNumber(user.getLicenceNumber())) {
            model.addAttribute("errorMsg", "Licence Number already registered.");
            return "register";
        }
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("errorMsg", "Email already registered.");
            return "register";
        }
        // Save without password (set in signup page)
        User temp = new User();
        temp.setLicenceNumber(user.getLicenceNumber());
        temp.setEmail(user.getEmail());
        temp.setFirstName(user.getFirstName());
        temp.setLastName(user.getLastName());
        temp.setMobileNumber(user.getMobileNumber());
        temp.setVillage(user.getVillage());
        temp.setMandal(user.getMandal());
        temp.setDistrict(user.getDistrict());
        temp.setState(user.getState());
        temp.setRole(user.getRole());
        temp.setPassword("UNSET"); // will be updated at signup
        temp.setApprovalStatus(User.ApprovalStatus.PENDING);
        temp.setEnabled(false);
        userService.save(temp);

        redirectAttributes.addFlashAttribute("successMsg",
            "Registration successful! Please complete signup by setting your password.");
        return "redirect:/signup";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@RequestParam String licenceNumber,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "Passwords do not match.");
            return "signup";
        }
        var userOpt = userService.findByLicenceNumber(licenceNumber);
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMsg", "Licence Number not found. Please register first.");
            return "signup";
        }
        User user = userOpt.get();
        if (!user.getEmail().equals(email)) {
            model.addAttribute("errorMsg", "Email does not match registered email.");
            return "signup";
        }
        userService.setPassword(licenceNumber, password);
        redirectAttributes.addFlashAttribute("successMsg", "Password set! Please wait for admin/dealer approval.");
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
