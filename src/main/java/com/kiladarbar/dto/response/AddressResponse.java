package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {
    private UUID       id;
    private String     label;
    private String     addressLine1;
    private String     addressLine2;
    private String     landmark;
    private String     city;
    private String     state;
    private String     pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean    isDefault;
}
