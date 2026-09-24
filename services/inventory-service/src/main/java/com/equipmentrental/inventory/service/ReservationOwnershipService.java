package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.ReservationEquipmentOwnershipResponse;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.repository.EquipmentReservationItemRepository;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationOwnershipService {

    private final EquipmentReservationRepository reservationRepository;
    private final EquipmentReservationItemRepository itemRepository;

    public ReservationEquipmentOwnershipResponse verify(
            Long rentalOrderId,
            Long equipmentId
    ) {

        EquipmentReservation reservation =
                reservationRepository
                        .findByRentalOrderId(rentalOrderId)
                        .orElse(null);

        if (reservation == null) {
            return new ReservationEquipmentOwnershipResponse(
                    false,
                    null,
                    rentalOrderId,
                    null,
                    null,
                    equipmentId
            );
        }

        boolean exists =
                itemRepository
                        .existsByReservationIdAndEquipmentId(
                                reservation.getId(),
                                equipmentId
                        );

        return new ReservationEquipmentOwnershipResponse(
                exists,
                reservation.getId(),
                rentalOrderId,
                reservation.getOrganizationId(),
                reservation.getBranchId(),
                equipmentId
        );
    }
}