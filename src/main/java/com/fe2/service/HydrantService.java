package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.UrlHelper;
import com.fe2.pojo.wasserkarte.WasserkarteInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class HydrantService {

    @Autowired
    private Configuration configuration;

    private final String baseUrl = "https://api.wasserkarte.info/2.0/getSurroundingWaterSources/";

    /**
     * Creates hydrants as markers with custom icons (if configured).
     * If anything goes wrong returns an empty String
     * @param lat
     * @param lng
     * @param numItems
     * @param range
     * @return markers parameter like &markers=...&markers=... or empty string
     */
    public String generateHydrantsAsMarkers(final double lat, final double lng, final int numItems, final int range)
    {
        Optional<WasserkarteInfoResponse> hydrants = getHydrants(lat, lng, numItems, range);

        if (hydrants.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        // First group hydrants by sourceTypeId
        Map<Long, String> hydrantsByType = new TreeMap<>();
        hydrants.get().getWaterSources().forEach(h -> {
            Long sourceTypeId = h.getSourceType();
            if (!hydrantsByType.containsKey(sourceTypeId)) {
                hydrantsByType.put(sourceTypeId, "");
            }

            String newVal = hydrantsByType.get(sourceTypeId) + "|" + h.getLatitude() + "," + h.getLongitude();
            hydrantsByType.put(sourceTypeId, newVal);
        });

        // Generate Markers (preferred with custom icon)
        int countCustomIcons = 0;
        var customIcons = configuration.getVerifiedWasserkarteInfoCustomIcons();
        for (var entry : hydrantsByType.entrySet()) {
            Long sourceType = entry.getKey();
            String marker;

            // Only up to 5 custom icons supported by API
            if (countCustomIcons <= 5 && customIcons.containsKey(sourceType)) {
                marker = "anchor:center|icon:" + customIcons.get(sourceType) + entry.getValue();
                countCustomIcons++;
            }
            else {
                String label = String.valueOf(sourceType);
                if (sourceType.equals(1L))
                    label = "O"; // Oberflurhydrant (Ü from Überflurhydrant is not supported, U already occupied)
                else if (sourceType.equals(2L))
                    label = "U"; // Unterflurhydrant
                else if (sourceType.equals(3L))
                    label = "W"; // Wasser!!!!

                marker = "color:blue|size:mid|label:" + label + entry.getValue();
            }

            result.append(UrlHelper.buildProperParameter("markers", marker));
        }

        return result.toString();
    }

    /**
     * Get Hydrants from Wasserkarte.Info
     * @param latitude
     * @param longitude
     * @param numItems
     * @param range
     * @return Optional response. Might be empty if no token present or on any other error.
     */
    private Optional<WasserkarteInfoResponse> getHydrants(double latitude, double longitude, int numItems, int range)
    {
        if (!configuration.isWasserkarteInfoApiEnabled())
            return Optional.empty();

        String url = baseUrl + "?source=alamosam";

        url += UrlHelper.buildProperParameter("token", configuration.getWasserkarteInfoToken());
        url += UrlHelper.buildProperParameter("lat", String.valueOf(latitude));
        url += UrlHelper.buildProperParameter("lng", String.valueOf(longitude));
        url += UrlHelper.buildProperParameter("range", String.valueOf(range));
        url += UrlHelper.buildProperParameter("numItems", String.valueOf(numItems));

        WasserkarteInfoResponse response;
        try {
            RestTemplate template = new RestTemplateBuilder().build();
            response = template.getForObject(url, WasserkarteInfoResponse.class);
            if (response == null)
                throw new IllegalStateException("Response is null");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            // Do not crash. Better empty map than no map at all...
            return Optional.empty();
        }

        return Optional.of(response);
    }
}
