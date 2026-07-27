package com.medical.repository;

import com.medical.model.DealerStock;
import com.medical.model.Medicine;
import com.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerStockRepository extends JpaRepository<DealerStock, Long> {
    List<DealerStock> findByDealer(User dealer);
    Optional<DealerStock> findByDealerAndMedicine(User dealer, Medicine medicine);
    List<DealerStock> findByDealerAndQuantityLessThan(User dealer, int quantity);
}
