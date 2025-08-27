package br.com.incode.nexus_bagde.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BadgeInfoDTO {

    private String id;
    private String name;
    private String criteria;
}
