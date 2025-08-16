package pl.ib.beauty.model.dto;

import lombok.Builder;

@Builder
public record AddressDtoRequest(String district,
                                String street,
                                String streetNumber,
                                String apartmentNumber,
                                String city,
                                String postalCode) {
}
