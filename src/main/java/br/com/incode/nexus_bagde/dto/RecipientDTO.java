package br.com.incode.nexus_bagde.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecipientDTO {

    private String type; // ex: "email"
    private String identity;
}
