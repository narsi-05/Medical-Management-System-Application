package com.medical.controller;

import com.medical.config.CustomUserDetails;
import com.medical.model.*;
import com.medical.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private MedicineService medicineService;
    @Autowired private TransactionService transactionService;
    @Autowired private SuggestionService suggestionService;
    @Autowired private StockService stockService;
    @Autowired private UserService userService;
    @Autowired private MedicineRequestService medicineRequestService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User user = ud.getUser();
        model.addAttribute("user", user);
        model.addAttribute("stockCount", stockService.getUserStock(user).size());
        model.addAttribute("lowStockCount", stockService.getUserLowStock(user).size());
        model.addAttribute("lowStockItems", stockService.getUserLowStock(user));
        model.addAttribute("recentTransactions",
                transactionService.getTransactionsByUser(user).stream().limit(5).toList());
        long pendingRequests = medicineRequestService.getRequestsByRequester(user)
                .stream().filter(r -> r.getStatus() == MedicineRequest.RequestStatus.PENDING).count();
        model.addAttribute("pendingRequests", pendingRequests);
        return "user/dashboard";
    }

    // ---- View Available Medicines ----
    @GetMapping("/medicines")
    public String medicines(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "user/medicines";
    }

    // ---- My Stock ----
    @GetMapping("/stock")
    public String stock(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        User user = ud.getUser();
        model.addAttribute("stockList", stockService.getUserStock(user));
        model.addAttribute("lowStockList", stockService.getUserLowStock(user));
        return "user/stock";
    }

    // ---- Sell / Dispense from stock ----
    @PostMapping("/stock/sell")
    public String sellFromStock(@RequestParam Long stockId,
                                 @RequestParam int quantity,
                                 @AuthenticationPrincipal CustomUserDetails ud,
                                 RedirectAttributes ra) {
        try {
            stockService.sellFromStock(ud.getUser(), stockId, quantity);
            ra.addFlashAttribute("successMsg", "✅ " + quantity + " units dispensed/sold from stock.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/user/stock";
    }

    // ---- Transactions ----
    @GetMapping("/transactions")
    public String transactions(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("transactions", transactionService.getTransactionsByUser(ud.getUser()));
        return "user/transactions";
    }

    // ---- BUY REQUEST: Show form ----
    @GetMapping("/buy")
    public String buyPage(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("dealers", userService.getAllApprovedDealers());
        model.addAttribute("myRequests", medicineRequestService.getRequestsByRequester(ud.getUser()));
        return "user/buy";
    }

    // ---- BUY REQUEST: Submit ----
    @PostMapping("/buy/request")
    public String submitRequest(@RequestParam Long dealerId,
                                 @RequestParam Long medicineId,
                                 @RequestParam int quantity,
                                 @RequestParam(required = false) String note,
                                 @AuthenticationPrincipal CustomUserDetails ud,
                                 RedirectAttributes ra) {
        try {
            User dealer = userService.findById(dealerId)
                    .orElseThrow(() -> new RuntimeException("Dealer not found"));
            Medicine medicine = medicineService.findById(medicineId)
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));
            medicineRequestService.placeRequest(ud.getUser(), dealer, medicine, quantity, note);
            ra.addFlashAttribute("successMsg", "✅ Request sent to dealer successfully! Awaiting approval.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "❌ Error: " + e.getMessage());
        }
        return "redirect:/user/buy";
    }

    // ---- Send Suggestion to Dealer ----
    @GetMapping("/suggestions")
    public String suggestionsPage(@AuthenticationPrincipal CustomUserDetails ud, Model model) {
        model.addAttribute("sent", suggestionService.getSentByUser(ud.getUser()));
        model.addAttribute("dealers", userService.getAllApprovedDealers());
        return "user/suggestions";
    }

    @PostMapping("/suggestions/send")
    public String sendSuggestion(@RequestParam Long dealerId,
                                  @RequestParam String subject,
                                  @RequestParam String message,
                                  @AuthenticationPrincipal CustomUserDetails ud,
                                  RedirectAttributes ra) {
        userService.findById(dealerId).ifPresent(dealer ->
            suggestionService.sendSuggestion(ud.getUser(), dealer, subject, message)
        );
        ra.addFlashAttribute("successMsg", "Suggestion sent to Dealer!");
        return "redirect:/user/suggestions";
    }
}
