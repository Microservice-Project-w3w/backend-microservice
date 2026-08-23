package com.equipmentrental.rental.service;

import com.equipmentrental.rental.client.InventoryClient;
import com.equipmentrental.rental.dto.response.RentalOwnershipResponse;
import com.equipmentrental.rental.entity.RentalOrder;
import com.equipmentrental.rental.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RentalOwnershipService {

    private final RentalOrderRepository rentalOrderRepository;
    private final InventoryClient inventoryClient;

    public RentalOwnershipResponse verify(
            Long rentalOrderId,
            Long equipmentId,
            Long currentUserId
    ) {

        log.info(
                "OWNERSHIP CHECK - rentalOrderId={}, equipmentId={}, currentUserId={}",
                rentalOrderId,
                equipmentId,
                currentUserId
        );

        RentalOrder order =
                rentalOrderRepository
                        .findById(rentalOrderId)
                        .orElse(null);

        /*
         * Không tìm thấy Rental Order
         */
        if (order == null) {

            log.warn(
                    "OWNERSHIP FAIL - Không tìm thấy RentalOrder id={}",
                    rentalOrderId
            );

            return new RentalOwnershipResponse(
                    false,
                    rentalOrderId,
                    currentUserId,
                    null,
                    null,
                    equipmentId
            );
        }

        log.info(
                "RENTAL ORDER FOUND - id={}, customerId={}, organizationId={}, branchId={}",
                order.getId(),
                order.getCustomerId(),
                order.getOrganizationId(),
                order.getBranchId()
        );

        /*
         * Kiểm tra CUSTOMER hiện tại có đúng chủ đơn thuê hay không
         */
        boolean customerMatches =
                order.getCustomerId() != null
                        && currentUserId != null
                        && order.getCustomerId().equals(currentUserId);

        log.info(
                "CUSTOMER CHECK - order.customerId={}, currentUserId={}, matches={}",
                order.getCustomerId(),
                currentUserId,
                customerMatches
        );

        if (!customerMatches) {

            log.warn(
                    "OWNERSHIP FAIL - Customer không khớp. order.customerId={}, currentUserId={}",
                    order.getCustomerId(),
                    currentUserId
            );

            return new RentalOwnershipResponse(
                    false,
                    order.getId(),
                    order.getCustomerId(),
                    order.getOrganizationId(),
                    order.getBranchId(),
                    equipmentId
            );
        }

        /*
         * Customer đúng.
         * Tiếp tục hỏi Inventory xem equipment có nằm trong
         * reservation của Rental Order này hay không.
         */
        InventoryClient.ReservationOwnershipResponse inventory =
                inventoryClient.verifyReservationEquipment(
                        rentalOrderId,
                        equipmentId
                );

        log.info(
                "INVENTORY OWNERSHIP RESPONSE = {}",
                inventory
        );

        boolean equipmentExists =
                inventory != null
                        && inventory.exists();

        log.info(
                "INVENTORY CHECK - exists={}",
                equipmentExists
        );

        /*
         * Chỉ owned=true khi:
         *
         * 1. Rental Order tồn tại
         * 2. Customer đúng chủ Rental Order
         * 3. Equipment thuộc reservation của Rental Order
         */
        boolean owned =
                customerMatches
                        && equipmentExists;

        log.info(
                "FINAL OWNERSHIP RESULT - owned={}",
                owned
        );

        return new RentalOwnershipResponse(
                owned,
                order.getId(),
                order.getCustomerId(),
                order.getOrganizationId(),
                order.getBranchId(),
                equipmentId
        );
    }
}