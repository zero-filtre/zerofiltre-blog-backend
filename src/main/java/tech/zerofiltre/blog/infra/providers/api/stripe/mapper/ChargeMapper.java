package tech.zerofiltre.blog.infra.providers.api.stripe.mapper;

import com.stripe.model.*;
import org.mapstruct.*;
import tech.zerofiltre.blog.domain.payment.model.*;

@Mapper
public interface ChargeMapper {
    ChargeResult fromCharge(Charge charge);
}
