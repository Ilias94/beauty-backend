package pl.ib.beauty.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

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
}
