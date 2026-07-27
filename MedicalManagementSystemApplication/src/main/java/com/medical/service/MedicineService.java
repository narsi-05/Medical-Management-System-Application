package com.medical.service;

import com.medical.model.Medicine;
import com.medical.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private static final int LOW_STOCK_THRESHOLD = 20;

    @Autowired
    private MedicineRepository medicineRepository;

    public Medicine save(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> findById(Long id) {
        return medicineRepository.findById(id);
    }

    public void deleteById(Long id) {
        medicineRepository.deleteById(id);
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findByQuantityLessThan(LOW_STOCK_THRESHOLD);
    }

    public List<Medicine> searchByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public Medicine updateQuantity(Long id, int quantity) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        medicine.setQuantity(medicine.getQuantity() + quantity);
        return medicineRepository.save(medicine);
    }
}
