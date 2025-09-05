package br.com.incode.nexus_bagde.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecipientProfileDTO {

    @JsonProperty("@context")
    private String context = "https://openbadgespec.org/extensions/recipientProfile/context.json";

    private List<String> type = List.of("Extension", "extensions:RecipientProfile");

    private String name;

    public RecipientProfileDTO(String name) {
        this.name = name;
    }
}
