package pl.ib.beauty.service;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.repository.AddressRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddressCoordinatesBackfillTest {

    private final AddressRepository addressRepository = mock(AddressRepository.class);
    private final GeoCodingService geoCodingService = mock(GeoCodingService.class);
    private final CacheManager cacheManager = mock(CacheManager.class);
    private final Cache courseCache = mock(Cache.class);

    private final AddressCoordinatesBackfill backfill =
            new AddressCoordinatesBackfill(addressRepository, geoCodingService, cacheManager);

    private final Address found = Address.builder().id(1L).street("Floriańska").streetNumber("12").city("Kraków").build();
    private final Address unknown = Address.builder().id(2L).street("Nieistniejąca").streetNumber("1").city("Kraków").build();

    @Test
    void savesTheCoordinatesGoogleFindsAndRefreshesCachedCourses() {
        when(addressRepository.findByLatIsNullAndStreetIsNotNullAndCityIsNotNull()).thenReturn(List.of(found, unknown));
        when(cacheManager.getCache("course")).thenReturn(courseCache);
        doAnswer(call -> {
            Address address = call.getArgument(0);
            if (address == found) {
                address.setLat(50.06);
                address.setLng(19.94);
            }
            return null;
        }).when(geoCodingService).locate(any());

        backfill.backfill();

        assertThat(found.getLat()).isEqualTo(50.06);
        verify(addressRepository).save(found);
        verify(addressRepository, never()).save(unknown);
        verify(courseCache).clear();
    }

    @Test
    void stopsAtTheFirstFailureInsteadOfFailingForEveryAddress() {
        when(addressRepository.findByLatIsNullAndStreetIsNotNullAndCityIsNotNull()).thenReturn(List.of(found, unknown));
        doThrow(new IllegalStateException("REQUEST_DENIED")).when(geoCodingService).locate(found);

        backfill.backfill();

        verify(geoCodingService, never()).locate(unknown);
        verify(addressRepository, never()).save(any());
        verify(cacheManager, never()).getCache(any());
    }
}
