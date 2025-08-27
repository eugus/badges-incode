package br.com.incode.nexus_bagde.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class BadgeValidationResponse {

    private boolean valid;
    private String message;
    private String issuer;
    private String recipient;
    private boolean notExpired;
}
