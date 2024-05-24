package tech.zerofiltre.blog.infra.providers.database.payment;

import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.infra.providers.database.payment.mapper.PaymentJPAMapper;

import java.util.Optional;

@Component
public class DBNotchPayProvider {

    private final PaymentJPARepository paymentJPARepository;
    private final PaymentJPAMapper mapper = Mappers.getMapper(PaymentJPAMapper.class);


    public DBNotchPayProvider(PaymentJPARepository paymentJPARepository) {
        this.paymentJPARepository = paymentJPARepository;
    }

    public Optional<Payment> paymentOf(String reference) {
        return paymentJPARepository.findByReference(reference)
                .map(mapper::fromJPA);
    }

    public tech.zerofiltre.blog.domain.payment.model.Payment save(tech.zerofiltre.blog.domain.payment.model.Payment payment) {
        return mapper.fromJPA(paymentJPARepository.save(mapper.toJPA(payment)));
    }
}
