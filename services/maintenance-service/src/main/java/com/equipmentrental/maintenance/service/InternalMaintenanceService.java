package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.InternalCreateMaintenanceRequest;
import com.equipmentrental.maintenance.dto.response.EquipmentMaintenanceStateResponse;
import com.equipmentrental.maintenance.dto.response.EquipmentRentalBlockResponse;
import com.equipmentrental.maintenance.dto.response.MaintenanceRequestResponse;
import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.mapper.MaintenanceRequestMapper;
import com.equipmentrental.maintenance.repository.MaintenanceRequestRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InternalMaintenanceService {

    private final MaintenanceRequestRepository requestRepository;

    private final MaintenanceWorkOrderRepository workOrderRepository;

    private final MaintenanceRequestMapper requestMapper;

    private final CurrentUserService currentUserService;

    public MaintenanceRequestResponse createFromLogistics(
            InternalCreateMaintenanceRequest dto
    ) {

        currentUserService.requireOrganization(
                dto.organizationId()
        );

        currentUserService.requireBranch(
                dto.branchId()
        );

        MaintenanceRequest entity =
                MaintenanceRequest.builder()

                        .requestCode(
                                "MR-" +
                                        UUID.randomUUID()
                                                .toString()
                                                .substring(0, 8)
                                                .toUpperCase()
                        )

                        .organizationId(
                                dto.organizationId()
                        )

                        .branchId(
                                dto.branchId()
                        )

                        .equipmentId(
                                dto.equipmentId()
                        )

                        .rentalOrderId(
                                dto.rentalOrderId()
                        )

                        .sourceReferenceId(
                                dto.sourceReferenceId()
                        )

                        .sourceType(
                                MaintenanceSourceType.LOGISTICS
                        )

                        .maintenanceType(
                                dto.maintenanceType()
                        )

                        .severity(
                                dto.severity()
                        )

                        .title(
                                dto.title()
                        )

                        .description(
                                dto.description()
                        )

                        .status(
                                MaintenanceRequestStatus.OPEN
                        )

                        .build();

        MaintenanceRequest saved =
                requestRepository.save(entity);

        return requestMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EquipmentMaintenanceStateResponse getState(
            Long equipmentId
    ) {

        boolean requestOpen =
                requestRepository
                        .existsByEquipmentIdAndStatusIn(
                                equipmentId,
                                List.of(
                                        MaintenanceRequestStatus.OPEN,
                                        MaintenanceRequestStatus.IN_PROGRESS,
                                        MaintenanceRequestStatus.CONVERTED_TO_WORK_ORDER
                                )
                        );

        boolean workOrderOpen =
                workOrderRepository
                        .existsByEquipmentIdAndStatusIn(
                                equipmentId,
                                List.of(
                                        WorkOrderStatus.OPEN,
                                        WorkOrderStatus.ASSIGNED,
                                        WorkOrderStatus.IN_PROGRESS,
                                        WorkOrderStatus.WAITING_PARTS,
                                        WorkOrderStatus.COMPLETED
                                )
                        );

        boolean underMaintenance =
                requestOpen || workOrderOpen;

        return new EquipmentMaintenanceStateResponse(
                equipmentId,
                underMaintenance,
                requestOpen ? "ACTIVE" : "NONE",
                workOrderOpen ? "ACTIVE" : "NONE"
        );
    }

    @Transactional(readOnly = true)
    public EquipmentRentalBlockResponse getRentalBlock(
            Long equipmentId
    ) {

        EquipmentMaintenanceStateResponse state =
                getState(equipmentId);

        if (state.underMaintenance()) {

            return new EquipmentRentalBlockResponse(
                    equipmentId,
                    true,
                    "Thiết bị đang có nghiệp vụ bảo trì/sửa chữa"
            );
        }

        return new EquipmentRentalBlockResponse(
                equipmentId,
                false,
                null
        );
    }
}
