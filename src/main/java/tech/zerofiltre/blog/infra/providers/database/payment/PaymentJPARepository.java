package tech.zerofiltre.blog.infra.providers.database.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zerofiltre.blog.infra.providers.database.payment.model.PaymentJPA;

import java.util.Optional;

public interface PaymentJPARepository extends JpaRepository<PaymentJPA, Long> {
    Optional<PaymentJPA> findByReference(String reference);
}
