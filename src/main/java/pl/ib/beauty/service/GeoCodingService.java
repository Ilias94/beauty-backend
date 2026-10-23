package pl.ib.beauty.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.ib.beauty.model.dao.Address;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GeoCodingService {
    private final GeoApiContext geoApiContext;

    @SneakyThrows
    public LatLng getGeoCoding(String address) {
        GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
        if (results.length > 0) {
            return results[0].geometry.location;
        }
        return null;
    }

    /** Sets the address's coordinates for the course map; leaves them as they are when Google finds nothing. */
    public void locate(Address address) {
        String query = queryFor(address);
        if (query.isEmpty()) {
            return;
        }
        LatLng latLng = getGeoCoding(query);
        if (latLng != null) {
            address.setLat(latLng.lat);
            address.setLng(latLng.lng);
        }
    }

    /**
     * "Floriańska 12, 31-000 Kraków": only the parts that are filled in, so a missing postal code is not sent
     * to Google as "null". The unit number is left out, since it does not move the pin.
     */
    static String queryFor(Address address) {
        return Stream.of(join(address.getStreet(), address.getStreetNumber()), join(address.getPostalCode(), address.getCity()))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private static String join(String... parts) {
        return Stream.of(parts)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(" "));
    }
}
