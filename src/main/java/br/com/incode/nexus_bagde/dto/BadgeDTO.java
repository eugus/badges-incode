package br.com.incode.nexus_bagde.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BadgeDTO {


    private Long id;

    @NotBlank(message = "Nome do badge é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    private String imagePath;

    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    private String category;

    @NotBlank(message = "Nome do emissor é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do emissor deve ter entre 2 e 100 caracteres")
    private String issuer;

    private String issuerImagePath;

    private Boolean isActive = true;

    // Campos Open Badges 2.0
    private RecipientDTO recipient;
    private BadgeInfoDTO badge;
    private Instant issuedOn;




}
