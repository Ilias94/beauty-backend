package pl.ib.beauty.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    private Long id;
    private String district;
    private String street;
    private String streetNumber;
    private String apartmentNumber;
    private String city;
    private String postalCode;
}
