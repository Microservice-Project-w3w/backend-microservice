package com.equipmentrental.inventory.service;

import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.exception.ResourceNotFoundException;
import com.equipmentrental.inventory.repository.EquipmentReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentReservationQueryService {

    private final EquipmentReservationRepository repository;

    @Transactional(readOnly = true)
    public List<EquipmentReservationResponse> findAll(
            Long organizationId,
            Long branchId,
            Long rentalOrderId,
            ReservationStatus status
    ) {
        if (organizationId == null) {
            throw new IllegalArgumentException(
                    "organizationId is required"
            );
        }

        List<EquipmentReservation> reservations;

        if (branchId != null) {
            reservations =
                    repository.findByOrganizationIdAndBranchId(
                            organizationId,
                            branchId
                    );
        } else {
            reservations =
                    repository.findByOrganizationId(
                            organizationId
                    );
        }

        return reservations
                .stream()
                .filter(reservation ->
                        rentalOrderId == null
                                || rentalOrderId.equals(
                                reservation.getRentalOrderId()
                        )
                )
                .filter(reservation ->
                        status == null
                                || status == reservation.getStatus()
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentReservationResponse findById(
            Long id
    ) {
        EquipmentReservation reservation =
                repository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found: "
                                                + id
                                )
                        );

        return toResponse(reservation);
    }

    private EquipmentReservationResponse toResponse(
            EquipmentReservation reservation
    ) {
        return new EquipmentReservationResponse(
                reservation.getId(),
                reservation.getOrganizationId(),
                reservation.getBranchId(),
                reservation.getReservationCode(),
                reservation.getRequestReference(),
                reservation.getRentalOrderId(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getExpiresAt(),
                reservation.getStatus(),
                reservation.getCreatedBy(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getVersion()
        );
    }
}