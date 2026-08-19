package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentQrService;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/equipment")
@RequiredArgsConstructor
public class EquipmentQrController {

    private final EquipmentQrService qrService;
    private final InternalEquipmentQueryService queryService;
    private final InventoryDataScopeGuard dataScopeGuard;
    private final EquipmentRepository equipmentRepository;

    @PostMapping("/{id}/qr")
    @PreAuthorize("hasAuthority('inventory.qr.generate')")
    public Object createQr(
            @PathVariable Long id
    ) {
        checkEquipmentScope(id);

        return qrService.create(id);
    }

    @GetMapping("/{id}/qr")
    @PreAuthorize("hasAuthority('inventory.qr.print')")
    public Object getQr(
            @PathVariable Long id
    ) {
        checkEquipmentScope(id);

        return qrService.get(id);
    }

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("hasAuthority('inventory.qr.print')")
    public Object findByQr(
            @PathVariable String qrCode
    ) {
        Equipment equipment =
                equipmentRepository
                        .findByQrCode(qrCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipment not found for QR: "
                                                + qrCode
                                )
                        );

        dataScopeGuard.checkBranch(
                equipment.getOrganizationId(),
                equipment.getBranchId()
        );

        return qrService.findByQr(qrCode);
    }

    @PostMapping("/{id}/qr/regenerate")
    @PreAuthorize("hasAuthority('inventory.qr.generate')")
    public Object regenerate(
            @PathVariable Long id
    ) {
        checkEquipmentScope(id);

        return qrService.regenerate(id);
    }

    private void checkEquipmentScope(
            Long equipmentId
    ) {
        InternalEquipmentResponse equipment =
                queryService.findById(
                        equipmentId
                );

        dataScopeGuard.checkBranch(
                equipment.organizationId(),
                equipment.branchId()
        );
    }
}