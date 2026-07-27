package com.medical.controller;

import com.medical.config.CustomUserDetails;
import com.medical.model.*;
import com.medical.repository.DealerStockRepository;
import com.medical.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private MedicineService medicineService;
    @Autowired private TransactionService transactionService;
    @Autowired private SuggestionService suggestionService;
    @Autowired private DealerStockRepository dealerStockRepository;
    @Autowired private MedicineRequestService medicineRequestService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User admin = ud.getUser();
        long pendingDealerRequests = medicineRequestService.countPendingForDealer(admin);
        model.addAttribute("admin", admin);
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("totalDealers", userService.getAllDealers().size());
        model.addAttribute("pendingDealers", userService.getPendingDealers().size());
        model.addAttribute("lowStockCount", medicineService.getLowStockMedicines().size());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        model.addAttribute("pendingDealerRequests", pendingDealerRequests);
        model.addAttribute("recentTransactions", transactionService.getAllTransactions()
                .stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ---- Medicine CRUD ----
    @GetMapping("/medicines")
    public String medicines(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("medicine", new Medicine());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        return "admin/medicines";
    }

    @PostMapping("/medicines/add")
    public String addMedicine(@ModelAttribute Medicine medicine, RedirectAttributes ra) {
        medicineService.save(medicine);
        ra.addFlashAttribute("successMsg", "Medicine added successfully!");
        return "redirect:/admin/medicines";
    }

    @GetMapping("/medicines/edit/{id}")
    public String editMedicineForm(@PathVariable Long id, Model model) {
        model.addAttribute("medicine", medicineService.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found")));
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("lowStockMedicines", medicineService.getLowStockMedicines());
        return "admin/medicines";
    }

    @PostMapping("/medicines/update/{id}")
    public String updateMedicine(@PathVariable Long id, @ModelAttribute Medicine medicine,
                                  RedirectAttributes ra) {
        medicine.setId(id);
        medicineService.save(medicine);
        ra.addFlashAttribute("successMsg", "Medicine updated successfully!");
        return "redirect:/admin/medicines";
    }

    @GetMapping("/medicines/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes ra) {
        medicineService.deleteById(id);
        ra.addFlashAttribute("successMsg", "Medicine deleted.");
        return "redirect:/admin/medicines";
    }

    // ---- Dealer Management ----
    @GetMapping("/dealers")
    public String dealers(Model model) {
        model.addAttribute("pendingDealers", userService.getPendingDealers());
        model.addAttribute("allDealers", userService.getAllDealers());
        return "admin/dealers";
    }

    @GetMapping("/dealers/approve/{id}")
    public String approveDealer(@PathVariable Long id, RedirectAttributes ra) {
        userService.approveUser(id);
        ra.addFlashAttribute("successMsg", "Dealer approved!");
        return "redirect:/admin/dealers";
    }

    @GetMapping("/dealers/reject/{id}")
    public String rejectDealer(@PathVariable Long id, RedirectAttributes ra) {
        userService.rejectUser(id);
        ra.addFlashAttribute("errorMsg", "Dealer rejected.");
        return "redirect:/admin/dealers";
    }

    // ---- Dealer Purchase Requests (Buy from Admin) ----
    @GetMapping("/requests")
    public String dealerRequests(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User admin = ud.getUser();
        model.addAttribute("pendingRequests", medicineRequestService.getPendingRequestsForDealer(admin));
        model.addAttribute("allRequests", medicineRequestService.getRequestsForDealer(admin));
        return "admin/requests";
    }

    @PostMapping("/requests/approve/{id}")
    public String approveDealerRequest(@PathVariable Long id,
                                        @RequestParam(required = false) String remark,
                                        RedirectAttributes ra) {
        try {
            medicineRequestService.adminApproveRequest(id, remark);
            ra.addFlashAttribute("successMsg", "✅ Request approved! Medicine supplied to dealer.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/requests/reject/{id}")
    public String rejectDealerRequest(@PathVariable Long id,
                                       @RequestParam(required = false) String remark,
                                       RedirectAttributes ra) {
        medicineRequestService.rejectRequest(id, remark);
        ra.addFlashAttribute("errorMsg", "Request rejected.");
        return "redirect:/admin/requests";
    }

    // ---- Supply Medicine to Dealer (manual) ----
    @GetMapping("/supply")
    public String supplyPage(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("dealers", userService.getAllApprovedDealers());
        return "admin/supply";
    }

    @PostMapping("/supply")
    public String supplyToDealer(@RequestParam Long dealerId,
                                  @RequestParam Long medicineId,
                                  @RequestParam int quantity,
                                  @AuthenticationPrincipal CustomUserDetails ud,
                                  RedirectAttributes ra) {
        try {
            User dealer = userService.findById(dealerId).orElseThrow();
            Medicine medicine = medicineService.findById(medicineId).orElseThrow();
            transactionService.adminSupplyToDealer(ud.getUser(), dealer, medicine, quantity);
            ra.addFlashAttribute("successMsg", "Medicine supplied to dealer successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/admin/supply";
    }

    // ---- All Users ----
    @GetMapping("/users")
    public String allUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // ---- Transactions ----
    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactions());
        return "admin/transactions";
    }

    // ---- Suggestions ----
    @GetMapping("/suggestions")
    public String suggestions(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("suggestions", suggestionService.getSuggestionsForUser(ud.getUser()));
        return "admin/suggestions";
    }
}
