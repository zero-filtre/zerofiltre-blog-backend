package tech.zerofiltre.blog.infra.providers.api.notchpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookData {
    private int amount;
    private int amountTotal;
    private boolean sandbox;
    private int fee;
    private int convertedAmount;
    private String paymentMethod;
    private Map<String, Object> metadata;
}

