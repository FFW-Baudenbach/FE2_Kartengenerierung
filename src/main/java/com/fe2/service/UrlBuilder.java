package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.UrlHelper;
import com.fe2.hydrants.WasserkarteInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class UrlBuilder {

    @Autowired
    private UrlSigner signer;

    @Autowired
    private HydrantService hydrantService;

    @Autowired
    private Configuration configuration;

    private final String baseUrl = "https://maps.googleapis.com/maps/api/staticmap";

    public URL generateOverviewUrl(final double lat, final double lng) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException {

        String url = baseUrl + "?center=" + lat + "," + lng;

        url += UrlHelper.buildProperParameter("size", "640x640");
        url += UrlHelper.buildProperParameter("zoom", "16");
        url += UrlHelper.buildProperParameter("scale", "2");
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", "roadmap"); // Streets
        url += UrlHelper.buildProperParameter("style", "feature:poi|visibility:off"); // Don't show POIs
        url += UrlHelper.buildProperParameter("style", "feature:transit|visibility:off"); // Don't show Transit symbols
        url += UrlHelper.buildProperParameter("markers", "color:red|" + lat + "," + lng); // Central marker
        url += generateHydrantsAsMarkers(lat, lng, 100, 500);

        return generateFinalUrl(url);
    }

    private String generateHydrantsAsMarkers(final double lat, final double lng, final int numItems, final int range)
    {
        Optional<WasserkarteInfoResponse> hydrants = hydrantService.getHydrants(lat, lng, numItems, range);

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
        var customIcons = hydrantService.getCustomIcons();
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

    private URL generateFinalUrl(final String url) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException
    {
        String finalUrl = url + UrlHelper.buildProperParameter("key", configuration.getGcpMapsApiKey());

        URL urlWithoutSignature = new URL(finalUrl);

        if (!signer.isSigningConfigured())
            return urlWithoutSignature;

        return signer.signUrl(urlWithoutSignature);
    }
}
