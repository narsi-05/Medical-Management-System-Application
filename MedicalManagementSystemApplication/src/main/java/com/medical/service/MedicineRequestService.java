package com.medical.service;

import com.medical.model.*;
import com.medical.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineRequestService {

    @Autowired
    private MedicineRequestRepository requestRepository;

    @Autowired
    private DealerStockRepository dealerStockRepository;

    @Autowired
    private UserStockRepository userStockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    // Hospital/Shop places a request to a Dealer
    public MedicineRequest placeRequest(User requester, User dealer, Medicine medicine,
                                         int quantity, String note) {
        MedicineRequest req = new MedicineRequest();
        req.setRequester(requester);
        req.setDealer(dealer);
        req.setMedicine(medicine);
        req.setRequestedQuantity(quantity);
        req.setNote(note);
        req.setStatus(MedicineRequest.RequestStatus.PENDING);
        req.setRequestedAt(LocalDateTime.now());
        return requestRepository.save(req);
    }

    // Dealer approves the request → transfer from dealer stock to requester stock
    @Transactional
    public MedicineRequest approveRequest(Long requestId, String remark) {
        MedicineRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Check dealer stock
        DealerStock ds = dealerStockRepository
                .findByDealerAndMedicine(req.getDealer(), req.getMedicine())
                .orElseThrow(() -> new RuntimeException("Medicine not in dealer stock"));

        if (ds.getQuantity() < req.getRequestedQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + ds.getQuantity());
        }

        // Deduct from dealer
        ds.setQuantity(ds.getQuantity() - req.getRequestedQuantity());
        dealerStockRepository.save(ds);

        // Add to requester stock
        UserStock us = userStockRepository
                .findByUserAndMedicine(req.getRequester(), req.getMedicine())
                .orElse(new UserStock());
        us.setUser(req.getRequester());
        us.setMedicine(req.getMedicine());
        us.setQuantity(us.getQuantity() + req.getRequestedQuantity());
        userStockRepository.save(us);

        // Create transaction record
        Transaction.TransactionType type = req.getRequester().getRole() == User.Role.HOSPITAL
                ? Transaction.TransactionType.DEALER_TO_HOSPITAL
                : Transaction.TransactionType.DEALER_TO_MEDICAL_SHOP;

        Transaction tx = new Transaction();
        tx.setMedicine(req.getMedicine());
        tx.setFromUser(req.getDealer());
        tx.setToUser(req.getRequester());
        tx.setQuantity(req.getRequestedQuantity());
        tx.setTotalPrice(req.getMedicine().getPrice() * req.getRequestedQuantity());
        tx.setType(type);
        transactionRepository.save(tx);

        // Update request status
        req.setStatus(MedicineRequest.RequestStatus.APPROVED);
        req.setDealerRemark(remark);
        req.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(req);
    }

    // Admin approves a dealer's purchase request → deduct from admin medicine stock, add to dealer stock
    @Transactional
    public MedicineRequest adminApproveRequest(Long requestId, String remark) {
        MedicineRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Medicine med = medicineRepository.findById(req.getMedicine().getId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (med.getQuantity() < req.getRequestedQuantity()) {
            throw new RuntimeException("Insufficient admin stock. Available: " + med.getQuantity());
        }

        // Deduct from admin medicine stock
        med.setQuantity(med.getQuantity() - req.getRequestedQuantity());
        medicineRepository.save(med);

        // Add to dealer stock
        DealerStock ds = dealerStockRepository
                .findByDealerAndMedicine(req.getRequester(), med)
                .orElse(new DealerStock());
        ds.setDealer(req.getRequester());
        ds.setMedicine(med);
        ds.setQuantity(ds.getQuantity() + req.getRequestedQuantity());
        dealerStockRepository.save(ds);

        // Create transaction record
        Transaction tx = new Transaction();
        tx.setMedicine(med);
        tx.setFromUser(req.getDealer()); // admin
        tx.setToUser(req.getRequester()); // dealer
        tx.setQuantity(req.getRequestedQuantity());
        tx.setTotalPrice(med.getPrice() * req.getRequestedQuantity());
        tx.setType(Transaction.TransactionType.ADMIN_TO_DEALER);
        transactionRepository.save(tx);

        req.setStatus(MedicineRequest.RequestStatus.APPROVED);
        req.setDealerRemark(remark);
        req.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(req);
    }

    // Dealer rejects the request
    public MedicineRequest rejectRequest(Long requestId, String remark) {
        MedicineRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus(MedicineRequest.RequestStatus.REJECTED);
        req.setDealerRemark(remark);
        req.setRespondedAt(LocalDateTime.now());
        return requestRepository.save(req);
    }

    public List<MedicineRequest> getRequestsByRequester(User requester) {
        return requestRepository.findByRequesterOrderByRequestedAtDesc(requester);
    }

    public List<MedicineRequest> getRequestsForDealer(User dealer) {
        return requestRepository.findByDealerOrderByRequestedAtDesc(dealer);
    }

    public List<MedicineRequest> getPendingRequestsForDealer(User dealer) {
        return requestRepository.findByDealerAndStatusOrderByRequestedAtDesc(
                dealer, MedicineRequest.RequestStatus.PENDING);
    }

    public long countPendingForDealer(User dealer) {
        return requestRepository.countByDealerAndStatus(dealer, MedicineRequest.RequestStatus.PENDING);
    }

    public Optional<MedicineRequest> findById(Long id) {
        return requestRepository.findById(id);
    }
}
