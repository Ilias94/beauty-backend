package pl.ib.beauty.service;

import org.junit.jupiter.api.Test;
import pl.ib.beauty.model.dao.Address;

import static org.assertj.core.api.Assertions.assertThat;

class GeoCodingServiceTest {

    @Test
    void asksGoogleForTheStreetAndTown() {
        Address address = Address.builder()
                .street("Floriańska").streetNumber("12").apartmentNumber("5")
                .postalCode("31-000").city("Kraków").district("Stare Miasto")
                .build();

        assertThat(GeoCodingService.queryFor(address)).isEqualTo("Floriańska 12, 31-000 Kraków");
    }

    @Test
    void leavesOutMissingPartsInsteadOfSendingNull() {
        Address address = Address.builder().street(" Floriańska ").streetNumber("12").postalCode("").city("Kraków").build();

        assertThat(GeoCodingService.queryFor(address)).isEqualTo("Floriańska 12, Kraków");
    }

    @Test
    void hasNothingToAskForAnEmptyAddress() {
        assertThat(GeoCodingService.queryFor(new Address())).isEmpty();
    }
}
