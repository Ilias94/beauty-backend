package pl.ib.beauty.controller;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/geolocation", produces = MediaType.APPLICATION_JSON_VALUE)
public class GeoLocationController {
    private final GeoApiContext geoApiContext;

    @PostMapping
    public void geoLocation() throws IOException, InterruptedException, ApiException {
        GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, "ul.Kościuszki 50, Wrocław, Polska").await();
        if (results.length > 0) {
            double lat = results[0].geometry.location.lat;
            double lng = results[0].geometry.location.lng;
            System.out.println(lat + " " + lng);
        }
    }
}
