package com.medical.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dealer_stock")
public class DealerStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private User dealer;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private int quantity;

    private static final int LOW_STOCK_THRESHOLD = 20;

    // ---- Constructors ----
    public DealerStock() {}

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDealer() { return dealer; }
    public void setDealer(User dealer) { this.dealer = dealer; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isLowStock() { return quantity < LOW_STOCK_THRESHOLD; }
}
