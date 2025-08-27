package br.com.incode.nexus_bagde.dto;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeValidationRequest {

    private JsonNode badgeJson;
    private String recipient;
}
