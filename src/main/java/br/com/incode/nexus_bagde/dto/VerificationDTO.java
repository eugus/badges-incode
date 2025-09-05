package br.com.incode.nexus_bagde.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDTO {
    private String type = "HostedBadge";
}
