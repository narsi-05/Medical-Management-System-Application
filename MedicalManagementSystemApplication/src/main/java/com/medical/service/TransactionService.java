package com.medical.service;

import com.medical.model.*;
import com.medical.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DealerStockRepository dealerStockRepository;

    @Autowired
    private UserStockRepository userStockRepository;

    // Admin supplies medicine to Dealer
    @Transactional
    public Transaction adminSupplyToDealer(User admin, User dealer, Medicine medicine, int qty) {
        Medicine med = medicineRepository.findById(medicine.getId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (med.getQuantity() < qty) throw new RuntimeException("Insufficient admin stock");

        med.setQuantity(med.getQuantity() - qty);
        medicineRepository.save(med);

        DealerStock stock = dealerStockRepository.findByDealerAndMedicine(dealer, med)
                .orElse(new DealerStock());
        stock.setDealer(dealer);
        stock.setMedicine(med);
        stock.setQuantity(stock.getQuantity() + qty);
        dealerStockRepository.save(stock);

        Transaction tx = new Transaction();
        tx.setMedicine(med);
        tx.setFromUser(admin);
        tx.setToUser(dealer);
        tx.setQuantity(qty);
        tx.setTotalPrice(med.getPrice() * qty);
        tx.setType(Transaction.TransactionType.ADMIN_TO_DEALER);
        return transactionRepository.save(tx);
    }

    // Dealer supplies medicine to Hospital or Medical Shop
    @Transactional
    public Transaction dealerSupplyToUser(User dealer, User buyer, Medicine medicine, int qty) {
        Medicine med = medicineRepository.findById(medicine.getId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        DealerStock dealerStock = dealerStockRepository.findByDealerAndMedicine(dealer, med)
                .orElseThrow(() -> new RuntimeException("Dealer does not have this medicine"));

        if (dealerStock.getQuantity() < qty) throw new RuntimeException("Insufficient dealer stock");

        dealerStock.setQuantity(dealerStock.getQuantity() - qty);
        dealerStockRepository.save(dealerStock);

        UserStock userStock = userStockRepository.findByUserAndMedicine(buyer, med)
                .orElse(new UserStock());
        userStock.setUser(buyer);
        userStock.setMedicine(med);
        userStock.setQuantity(userStock.getQuantity() + qty);
        userStockRepository.save(userStock);

        Transaction.TransactionType type = buyer.getRole() == User.Role.HOSPITAL
                ? Transaction.TransactionType.DEALER_TO_HOSPITAL
                : Transaction.TransactionType.DEALER_TO_MEDICAL_SHOP;

        Transaction tx = new Transaction();
        tx.setMedicine(med);
        tx.setFromUser(dealer);
        tx.setToUser(buyer);
        tx.setQuantity(qty);
        tx.setTotalPrice(med.getPrice() * qty);
        tx.setType(type);
        return transactionRepository.save(tx);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByFromUserOrToUser(user, user);
    }
}
