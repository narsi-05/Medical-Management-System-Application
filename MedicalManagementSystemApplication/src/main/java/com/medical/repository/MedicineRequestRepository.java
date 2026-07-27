package com.medical.repository;

import com.medical.model.MedicineRequest;
import com.medical.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRequestRepository extends JpaRepository<MedicineRequest, Long> {

    List<MedicineRequest> findByRequesterOrderByRequestedAtDesc(User requester);

    List<MedicineRequest> findByDealerOrderByRequestedAtDesc(User dealer);

    List<MedicineRequest> findByDealerAndStatusOrderByRequestedAtDesc(User dealer, MedicineRequest.RequestStatus status);

    long countByDealerAndStatus(User dealer, MedicineRequest.RequestStatus status);

    List<MedicineRequest> findByRequesterAndDealerOrderByRequestedAtDesc(User requester, User dealer);
}
