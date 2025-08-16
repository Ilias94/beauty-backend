package pl.ib.beauty.model.dto;

public record AddressDtoResponse(Long id,
                                 String district,
                                 String street,
                                 String streetNumber,
                                 String apartmentNumber,
                                 String city,
                                 String postalCode,
                                 double lat,
                                 double lng) {
}
