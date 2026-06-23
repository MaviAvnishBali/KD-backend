package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAddressRequest {
    private  String     label;
    @NotBlank private String addressLine1;
    private  String     addressLine2;
    private  String     landmark;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank @Size(min = 6, max = 6) private String pincode;
    private  BigDecimal latitude;
    private  BigDecimal longitude;
}
