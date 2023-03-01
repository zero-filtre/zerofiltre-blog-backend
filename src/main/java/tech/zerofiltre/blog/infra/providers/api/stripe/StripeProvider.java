package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.*;
import com.stripe.model.*;
import lombok.extern.slf4j.*;
import org.mapstruct.factory.*;
import org.springframework.stereotype.*;
import tech.zerofiltre.blog.domain.payment.*;
import tech.zerofiltre.blog.domain.payment.model.*;
import tech.zerofiltre.blog.infra.providers.api.stripe.mapper.*;

import java.util.*;

@Slf4j
@Component
public class StripeProvider implements PaymentProvider {


    private final ChargeMapper chargeMapper = Mappers.getMapper(ChargeMapper.class);


    @Override
    public ChargeResult charge(ChargeRequest chargeRequest) throws PaymentException {
        try {
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", chargeRequest.getAmount());
            chargeParams.put("currency", chargeRequest.getCurrency());
            chargeParams.put("description", chargeRequest.getDescription());
            chargeParams.put("source", chargeRequest.getToken());
            return chargeMapper.fromCharge(Charge.create(chargeParams));
        } catch (StripeException e) {
            throw new PaymentException("Error while charging card: " + e.getLocalizedMessage(), e, "");
        }
    }
}
