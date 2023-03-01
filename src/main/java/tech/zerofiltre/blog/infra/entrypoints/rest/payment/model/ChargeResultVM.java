package tech.zerofiltre.blog.infra.entrypoints.rest.payment.model;

import lombok.*;
import tech.zerofiltre.blog.domain.payment.model.*;

@Data
public class ChargeResultVM {

    private String chargeId;
    private String status;

    public ChargeResultVM(ChargeResult chargeResult) {
        this.chargeId = chargeResult.getId();
        this.status = chargeResult.getStatus();
    }
}
