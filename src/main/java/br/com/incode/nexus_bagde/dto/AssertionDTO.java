package br.com.incode.nexus_bagde.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssertionDTO {

    @JsonProperty("@context")
    private String context = "https://w3id.org/openbadges/v2";

    private String type = "Assertion";

    private String id;

    private RecipientDTO recipient;

    private String badge;

    private String issuedOn;

    private ImageDTO image;

    private List<EvidenceDTO> evidence;

    private String narrative;

    private boolean revoked;

    private VerificationDTO verification;

    @JsonProperty("extensions:recipientProfile")
    private RecipientProfileDTO recipientProfile;
}
