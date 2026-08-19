package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.request.CreateStatusHistoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentStatusHistoryResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.entity.EquipmentStatusHistory;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.mapper.EquipmentStatusHistoryMapper;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.repository.EquipmentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipmentStatusHistoryService {

    private static final Set<EquipmentStatus> ALLOWED_ADMIN_STATUSES =
            EnumSet.of(
                    EquipmentStatus.DAMAGED,
                    EquipmentStatus.MAINTENANCE,
                    EquipmentStatus.LOST,
                    EquipmentStatus.RETIRED,
                    EquipmentStatus.INSPECTION
            );

    private final EquipmentStatusHistoryRepository historyRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentStatusHistoryResponse create(
            Long equipmentId,
            CreateStatusHistoryRequest request,
            Long changedBy
    ) {
        if (request == null
                || request.newStatus() == null) {
            throw new IllegalArgumentException(
                    "newStatus is required"
            );
        }

        if (!ALLOWED_ADMIN_STATUSES.contains(
                request.newStatus()
        )) {
            throw new IllegalArgumentException(
                    "Status cannot be changed by generic status-history endpoint"
            );
        }

        Equipment equipment =
                equipmentRepository
                        .findById(equipmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found"
                                )
                        );

        EquipmentStatus oldStatus =
                equipment.getStatus();

        equipment.setStatus(
                request.newStatus()
        );

        equipmentRepository.save(
                equipment
        );

        EquipmentStatusHistory history =
                EquipmentStatusHistory.builder()
                        .equipmentId(equipmentId)
                        .oldStatus(oldStatus)
                        .newStatus(request.newStatus())
                        .reason(request.reason())
                        .changedBy(changedBy)
                        .build();

        return EquipmentStatusHistoryMapper.toResponse(
                historyRepository.save(history)
        );
    }

    public List<EquipmentStatusHistoryResponse> findAll(
            Long equipmentId
    ) {
        return historyRepository
                .findByEquipmentIdOrderByChangedAtDesc(
                        equipmentId
                )
                .stream()
                .map(
                        EquipmentStatusHistoryMapper::toResponse
                )
                .toList();
    }
}