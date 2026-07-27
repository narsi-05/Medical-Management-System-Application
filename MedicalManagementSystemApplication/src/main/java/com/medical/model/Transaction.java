package com.medical.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;   // seller (admin or dealer)

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;     // buyer (dealer, hospital, or medical shop)

    private int quantity;
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private LocalDateTime transactionDate = LocalDateTime.now();

    public enum TransactionType {
        ADMIN_TO_DEALER,
        DEALER_TO_HOSPITAL,
        DEALER_TO_MEDICAL_SHOP
    }

    // ---- Constructors ----
    public Transaction() {}

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}
