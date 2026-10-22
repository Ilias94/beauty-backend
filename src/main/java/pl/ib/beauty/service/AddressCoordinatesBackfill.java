package pl.ib.beauty.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.ib.beauty.model.dao.Address;
import pl.ib.beauty.repository.AddressRepository;

import java.util.List;

/**
 * Courses saved before their address was geocoded have no coordinates, so their page shows no map.
 * After startup this looks those addresses up once; later ones are geocoded when the course is saved.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "google.backfill-coordinates", havingValue = "true", matchIfMissing = true)
public class AddressCoordinatesBackfill {
    private final AddressRepository addressRepository;
    private final GeoCodingService geoCodingService;
    private final CacheManager cacheManager;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void backfill() {
        List<Address> missing = addressRepository.findByLatIsNullAndStreetIsNotNullAndCityIsNotNull();
        int located = 0;
        for (Address address : missing) {
            try {
                geoCodingService.locate(address);
            } catch (Exception e) {
                // Usually a missing or restricted API key: the remaining addresses would fail the same way.
                log.warn("Stopped filling in map coordinates at address {}: {}", address.getId(), e.getMessage());
                break;
            }
            if (address.getLat() != null) {
                addressRepository.save(address);
                located++;
            }
        }
        if (located > 0) {
            // Cached courses still carry the old, coordinate-less address.
            Cache courses = cacheManager.getCache("course");
            if (courses != null) {
                courses.clear();
            }
            log.info("Filled in map coordinates for {} of {} addresses", located, missing.size());
        }
    }
}
