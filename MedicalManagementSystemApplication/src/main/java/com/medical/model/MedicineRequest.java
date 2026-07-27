package com.medical.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_requests")
public class MedicineRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;          // Hospital or Medical Shop

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private User dealer;             // Target Dealer

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private int requestedQuantity;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    private String note;             // Optional note from requester
    private String dealerRemark;     // Remark from dealer on approve/reject

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime respondedAt;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getDealer() { return dealer; }
    public void setDealer(User dealer) { this.dealer = dealer; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDealerRemark() { return dealerRemark; }
    public void setDealerRemark(String dealerRemark) { this.dealerRemark = dealerRemark; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
