package com.equipmentrental.billing.repository;

import com.equipmentrental.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentReference(String paymentReference);

    @Query("select payment from Payment payment join fetch payment.invoice where payment.id = :id")
    Optional<Payment> findByIdWithInvoice(@Param("id") Long id);
}
