package com.medical.controller;

import com.medical.config.CustomUserDetails;
import com.medical.model.*;
import com.medical.repository.UserRepository;
import com.medical.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dealer")
public class DealerController {

    @Autowired private UserService userService;
    @Autowired private MedicineService medicineService;
    @Autowired private TransactionService transactionService;
    @Autowired private SuggestionService suggestionService;
    @Autowired private StockService stockService;
    @Autowired private UserRepository userRepository;
    @Autowired private MedicineRequestService medicineRequestService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User dealer = ud.getUser();
        List<DealerStock> stock = stockService.getDealerStock(dealer);
        List<DealerStock> lowStock = stockService.getDealerLowStock(dealer);
        long pendingRequests = medicineRequestService.countPendingForDealer(dealer);
        // Count pending buy-from-admin requests for this dealer
        User admin = userService.findByLicenceNumber("ADMIN001").orElse(null);
        long pendingBuyRequests = (admin != null) ?
                medicineRequestService.getRequestsByRequester(dealer).stream()
                    .filter(r -> r.getStatus() == MedicineRequest.RequestStatus.PENDING).count() : 0;
        model.addAttribute("dealer", dealer);
        model.addAttribute("stockCount", stock.size());
        model.addAttribute("lowStockCount", lowStock.size());
        model.addAttribute("lowStockItems", lowStock);
        model.addAttribute("pendingUsers", userService.getPendingUsersForDealer().size());
        model.addAttribute("pendingMedRequests", pendingRequests);
        model.addAttribute("pendingBuyRequests", pendingBuyRequests);
        model.addAttribute("recentTransactions",
                transactionService.getTransactionsByUser(dealer).stream().limit(5).toList());
        return "dealer/dashboard";
    }

    // ---- Stock ----
    @GetMapping("/stock")
    public String stock(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User dealer = ud.getUser();
        model.addAttribute("stockList", stockService.getDealerStock(dealer));
        model.addAttribute("lowStockList", stockService.getDealerLowStock(dealer));
        return "dealer/stock";
    }

    // ---- Medicine Requests from Hospital/Shop ----
    @GetMapping("/requests")
    public String viewRequests(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User dealer = ud.getUser();
        model.addAttribute("pendingRequests", medicineRequestService.getPendingRequestsForDealer(dealer));
        model.addAttribute("allRequests", medicineRequestService.getRequestsForDealer(dealer));
        model.addAttribute("dealerStock", stockService.getDealerStock(dealer));
        return "dealer/requests";
    }

    @PostMapping("/requests/approve/{id}")
    public String approveRequest(@PathVariable Long id,
                                  @RequestParam(required = false) String remark,
                                  RedirectAttributes ra) {
        try {
            medicineRequestService.approveRequest(id, remark);
            ra.addFlashAttribute("successMsg", "✅ Request approved and medicine supplied!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/dealer/requests";
    }

    @PostMapping("/requests/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                                 @RequestParam(required = false) String remark,
                                 RedirectAttributes ra) {
        medicineRequestService.rejectRequest(id, remark);
        ra.addFlashAttribute("errorMsg", "Request rejected.");
        return "redirect:/dealer/requests";
    }

    // ---- Buy from Admin page ----
    @GetMapping("/buy")
    public String buyPage(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("myRequests", medicineRequestService.getRequestsByRequester(ud.getUser()));
        return "dealer/buy";
    }

    // ---- Submit buy request to Admin ----
    @PostMapping("/buy/request")
    public String submitBuyRequest(@RequestParam Long medicineId,
                                    @RequestParam int quantity,
                                    @RequestParam(required = false) String note,
                                    @AuthenticationPrincipal CustomUserDetails ud,
                                    RedirectAttributes ra) {
        try {
            User dealer = ud.getUser();
            Medicine medicine = medicineService.findById(medicineId)
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));
            User admin = userService.findByLicenceNumber("ADMIN001")
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            medicineRequestService.placeRequest(dealer, admin, medicine, quantity, note);
            ra.addFlashAttribute("successMsg", "✅ Purchase request sent to Admin successfully! Awaiting approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "❌ Error: " + e.getMessage());
        }
        return "redirect:/dealer/buy";
    }

    // ---- Manual Supply to Hospital/Shop ----
    @GetMapping("/supply")
    public String supplyPage(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User dealer = ud.getUser();
        model.addAttribute("dealerStock", stockService.getDealerStock(dealer));
        List<User> hospitals = userRepository.findByRoleAndApprovalStatus(
                User.Role.HOSPITAL, User.ApprovalStatus.APPROVED);
        List<User> shops = userRepository.findByRoleAndApprovalStatus(
                User.Role.MEDICAL_SHOP, User.ApprovalStatus.APPROVED);
        model.addAttribute("hospitals", hospitals);
        model.addAttribute("shops", shops);
        return "dealer/supply";
    }

    @PostMapping("/supply")
    public String supplyToUser(@RequestParam Long userId,
                                @RequestParam Long medicineId,
                                @RequestParam int quantity,
                                @AuthenticationPrincipal CustomUserDetails ud,
                                RedirectAttributes ra) {
        try {
            User buyer = userService.findById(userId).orElseThrow();
            Medicine medicine = medicineService.findById(medicineId).orElseThrow();
            transactionService.dealerSupplyToUser(ud.getUser(), buyer, medicine, quantity);
            ra.addFlashAttribute("successMsg", "Medicine supplied successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/dealer/supply";
    }

    // ---- Approve Users ----
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("pendingUsers", userService.getPendingUsersForDealer());
        model.addAttribute("approvedHospitals", userRepository.findByRoleAndApprovalStatus(
                User.Role.HOSPITAL, User.ApprovalStatus.APPROVED));
        model.addAttribute("approvedShops", userRepository.findByRoleAndApprovalStatus(
                User.Role.MEDICAL_SHOP, User.ApprovalStatus.APPROVED));
        return "dealer/users";
    }

    @GetMapping("/users/approve/{id}")
    public String approveUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.approveUser(id);
        ra.addFlashAttribute("successMsg", "User approved!");
        return "redirect:/dealer/users";
    }

    @GetMapping("/users/reject/{id}")
    public String rejectUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.rejectUser(id);
        ra.addFlashAttribute("errorMsg", "User rejected.");
        return "redirect:/dealer/users";
    }

    // ---- Suggestions ----
    @GetMapping("/suggestions")
    public String suggestions(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("suggestions", suggestionService.getSuggestionsForUser(ud.getUser()));
        return "dealer/suggestions";
    }

    @GetMapping("/suggestions/send")
    public String sendSuggestionPage(Model model) {
        return "dealer/send-suggestion";
    }

    @PostMapping("/suggestions/send")
    public String sendSuggestion(@RequestParam String subject,
                                  @RequestParam String message,
                                  @AuthenticationPrincipal CustomUserDetails ud,
                                  RedirectAttributes ra) {
        userService.findByLicenceNumber("ADMIN001").ifPresent(admin ->
                suggestionService.sendSuggestion(ud.getUser(), admin, subject, message));
        ra.addFlashAttribute("successMsg", "Suggestion sent to Admin!");
        return "redirect:/dealer/suggestions";
    }

    // ---- Transactions ----
    @GetMapping("/transactions")
    public String transactions(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("transactions", transactionService.getTransactionsByUser(ud.getUser()));
        return "dealer/transactions";
    }
}
