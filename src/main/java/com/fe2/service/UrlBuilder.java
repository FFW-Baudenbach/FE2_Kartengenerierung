package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.UrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UrlBuilder {

    @Autowired
    private UrlSigner signer;

    @Autowired
    private HydrantService hydrantService;

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private Configuration configuration;

    private final String baseUrl = "https://maps.googleapis.com/maps/api/staticmap";

    public URL generateOverviewRoadmapUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        String url = baseUrl + "?size=" + sizeParam;

        url += UrlHelper.buildProperParameter("scale", "2");
        url += UrlHelper.buildProperParameter("center", lat + "," + lng);
        url += UrlHelper.buildProperParameter("zoom", "16");
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", "roadmap"); // Streets
        url += UrlHelper.buildProperParameter("style", "feature:poi|visibility:off"); // Don't show POIs
        url += UrlHelper.buildProperParameter("style", "feature:transit|visibility:off"); // Don't show Transit symbols
        url += UrlHelper.buildProperParameter("markers", "color:red|size:mid|" + lat + "," + lng); // Destination
        url += hydrantService.generateHydrantsAsMarkers(lat, lng, 100, 0.5, true, false);

        Optional<String> route = destinationService.getEncodedPolylines(lat, lng);
        if (route.isPresent())
            url += UrlHelper.buildProperParameter("path", "color:0x0000ff60|weight:5|enc:" + route.get());

        return authorizeStaticMapsApiUrl(url);
    }

    public URL generateDetailHybridUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        String url = baseUrl + "?size=" + sizeParam;

        url += UrlHelper.buildProperParameter("scale", "2");
        url += UrlHelper.buildProperParameter("center", lat + "," + lng);
        url += UrlHelper.buildProperParameter("zoom", "18");
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", "hybrid"); // Sattelite + Streets
        url += UrlHelper.buildProperParameter("style", "feature:poi|visibility:off"); // Don't show POIs
        url += UrlHelper.buildProperParameter("style", "feature:transit|visibility:off"); // Don't show Transit symbols
        url += UrlHelper.buildProperParameter("markers", "color:white|size:mid|" + lat + "," + lng); // Destination
        url += hydrantService.generateHydrantsAsMarkers(lat, lng, 100, 0.5, false, false);

        Optional<String> route = destinationService.getEncodedPolylines(lat, lng);
        if (route.isPresent())
            url += UrlHelper.buildProperParameter("path", "color:0x0000ff80|weight:5|enc:" + route.get());

        return authorizeStaticMapsApiUrl(url);
    }

    public URL generateRouteRoadmapUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, InvalidKeyException, NoSuchAlgorithmException, URISyntaxException {
        String url = baseUrl + "?size=" + sizeParam;

        url += UrlHelper.buildProperParameter("scale", "2");
        // No center or zoom required, use implicit positioning by markers and path
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", "roadmap"); // Streets
        url += UrlHelper.buildProperParameter("style", "feature:poi|visibility:off"); // Don't show POIs
        url += UrlHelper.buildProperParameter("style", "feature:transit|visibility:off"); // Don't show Transit symbols
        url += UrlHelper.buildProperParameter("markers", "color:white|size:tiny|" + configuration.getGcpDirectionsOriginLat() + "," + configuration.getGcpDirectionsOriginLng()); // Origin
        url += UrlHelper.buildProperParameter("markers", "color:red|size:mid|" + lat + "," + lng); // Destination
        url += hydrantService.generateHydrantsAsMarkers(lat, lng, 100, 2.5, true, true);

        Optional<String> route = destinationService.getEncodedPolylines(lat, lng);
        if (route.isPresent())
            url += UrlHelper.buildProperParameter("path", "color:0x0000ff60|weight:5|enc:" + route.get());

        return authorizeStaticMapsApiUrl(url);
    }

    public URL generateGenericMapUrl(final MultiValueMap<String, String> parameters, boolean showHydrants, boolean showRoute, boolean showPois)
            throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        if (parameters.containsKey("center")) {
            throw new RuntimeException("Please use Parameters lat and lng instead of center");
        }

        String size = getSingleParameterValue(parameters, "size", false);
        String scale = getSingleParameterValue(parameters, "scale", true);
        String lat = getSingleParameterValue(parameters, "lat", false);
        String lng = getSingleParameterValue(parameters, "lng", false);
        String zoom = getSingleParameterValue(parameters, "zoom", false);
        String mapType = getSingleParameterValue(parameters, "maptype", false);
        String language = getSingleParameterValue(parameters, "language", true);
        String region = getSingleParameterValue(parameters, "region", true);

        List<String> styles = getMultipleParameterValues(parameters, "style", true);
        List<String> markers = getMultipleParameterValues(parameters, "markers", true);

        if (!showPois) {
            styles.add("feature:poi|visibility:off"); //Don't show Pois
            styles.add("feature:transit|visibility:off"); // Don't show Transit symbols
        }

        String url = baseUrl + "?size=" + size;
        url += UrlHelper.buildProperParameter("scale", scale);
        url += UrlHelper.buildProperParameter("center", lat + "," + lng);
        url += UrlHelper.buildProperParameter("zoom", zoom);
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", mapType);
        url += UrlHelper.buildProperParameter("language", language);
        url += UrlHelper.buildProperParameter("region", region);

        for (String style : styles) {
            url += UrlHelper.buildProperParameter("style", style);
        }
        for (String marker : markers) {
            url += UrlHelper.buildProperParameter("markers", marker);
        }

        if (showHydrants) {
            url += hydrantService.generateHydrantsAsMarkers(Double.parseDouble(lat), Double.parseDouble(lng), 100, 0.5, true, false);
        }

        if (showRoute) {
            Optional<String> route = destinationService.getEncodedPolylines(Double.parseDouble(lat), Double.parseDouble(lng));
            if (route.isPresent())
                url += UrlHelper.buildProperParameter("path", "color:0x0000ff60|weight:5|enc:" + route.get());
        }

        return authorizeStaticMapsApiUrl(url);
    }

    private String getSingleParameterValue(final MultiValueMap<String, String> parameters, String key, boolean optional) {
        if (!parameters.containsKey(key)) {
            if (optional)
                return null;
            throw new RuntimeException("Missing mandatory URL Parameter: " + key);
        }
        var values = parameters.get(key);
        if (values.size() != 1) {
            throw new RuntimeException("Exactly one URL Parameter with Key expected: " + key);
        }
        return parameters.getFirst(key);
    }

    private List<String> getMultipleParameterValues(final MultiValueMap<String, String> parameters, String key, boolean optional) {
        if (!parameters.containsKey(key)) {
            if (optional)
                return new ArrayList<>();
            throw new RuntimeException("Missing mandatory URL Parameter: " + key);
        }
        return parameters.get(key);
    }

    private URL authorizeStaticMapsApiUrl(final String url) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        String finalUrl = url + UrlHelper.buildProperParameter("key", configuration.getGcpMapsApiKey());

        URL urlWithoutSignature = new URI(finalUrl).toURL();

        if (!configuration.isSigningEnabled())
            return urlWithoutSignature;

        return signer.signUrl(urlWithoutSignature);
    }
}
