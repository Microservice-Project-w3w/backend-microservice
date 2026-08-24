package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class InternalEquipmentStatusService {

    private final EquipmentRepository equipmentRepository;

    public void changeStatus(
            Long equipmentId,
            String status
    ) {

        System.out.println(
                "========== INVENTORY STATUS DEBUG =========="
        );
        System.out.println(
                "equipmentId = " + equipmentId
        );
        System.out.println(
                "status received = [" + status + "]"
        );
        System.out.println(
                "============================================"
        );

        Equipment equipment = equipmentRepository
                .findById(equipmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thiết bị id=" + equipmentId
                        )
                );

        EquipmentStatus newStatus;

        try {
            newStatus = EquipmentStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Trạng thái thiết bị không hợp lệ: " + status
            );
        }

        equipment.setStatus(newStatus);

        equipmentRepository.save(equipment);
    }
}