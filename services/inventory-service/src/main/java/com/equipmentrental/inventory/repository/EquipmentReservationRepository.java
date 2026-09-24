package com.equipmentrental.inventory.repository;

import com.equipmentrental.inventory.entity.EquipmentReservation;
import com.equipmentrental.inventory.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentReservationRepository
        extends JpaRepository<EquipmentReservation, Long> {

    Optional<EquipmentReservation>
    findByRentalOrderId(Long rentalOrderId);

    Optional<EquipmentReservation>
    findByOrganizationIdAndRequestReference(
            Long organizationId,
            String requestReference
    );

    List<EquipmentReservation>
    findByStatus(
            ReservationStatus status
    );

    List<EquipmentReservation>
    findByRentalOrderIdAndStatus(
            Long rentalOrderId,
            ReservationStatus status
    );
    List<EquipmentReservation> findByOrganizationIdAndBranchId(
            Long organizationId,
            Long branchId
    );

    List<EquipmentReservation> findByOrganizationId(
            Long organizationId
    );

    boolean existsByOrganizationIdAndRequestReference(
            Long organizationId,
            String requestReference
    );
}