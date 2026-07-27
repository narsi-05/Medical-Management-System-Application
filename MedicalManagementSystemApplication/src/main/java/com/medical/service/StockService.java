package com.medical.service;

import com.medical.model.DealerStock;
import com.medical.model.User;
import com.medical.model.UserStock;
import com.medical.repository.DealerStockRepository;
import com.medical.repository.UserStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private static final int LOW_STOCK_THRESHOLD = 20;

    @Autowired
    private DealerStockRepository dealerStockRepository;

    @Autowired
    private UserStockRepository userStockRepository;

    public List<DealerStock> getDealerStock(User dealer) {
        return dealerStockRepository.findByDealer(dealer);
    }

    public List<DealerStock> getDealerLowStock(User dealer) {
        return dealerStockRepository.findByDealerAndQuantityLessThan(dealer, LOW_STOCK_THRESHOLD);
    }

    public List<UserStock> getUserStock(User user) {
        return userStockRepository.findByUser(user);
    }

    public List<UserStock> getUserLowStock(User user) {
        return userStockRepository.findByUserAndQuantityLessThan(user, LOW_STOCK_THRESHOLD);
    }

    @Transactional
    public void sellFromStock(User user, Long stockId, int quantity) {
        UserStock us = userStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock entry not found"));

        if (!us.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized action");
        }
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }
        if (us.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + us.getQuantity());
        }

        us.setQuantity(us.getQuantity() - quantity);
        userStockRepository.save(us);
    }
}
