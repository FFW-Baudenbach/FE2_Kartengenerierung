package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.UrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public URL generateOverviewRoadmapUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException
    {
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

    public URL generateDetailHybridUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException
    {
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

    public URL generateRouteRoadmapUrl(final double lat, final double lng, String sizeParam) throws MalformedURLException, InvalidKeyException, NoSuchAlgorithmException
    {
        String url = baseUrl + "?size=" + sizeParam;

        url += UrlHelper.buildProperParameter("scale", "2");
        // No center or zoom required, use implicit positioning by markers and path
        url += UrlHelper.buildProperParameter("format", configuration.getOutputFormat());
        url += UrlHelper.buildProperParameter("maptype", "roadmap"); // Streets
        url += UrlHelper.buildProperParameter("style", "feature:poi|visibility:off"); // Don't show POIs
        url += UrlHelper.buildProperParameter("style", "feature:transit|visibility:off"); // Don't show Transit symbols
        url += UrlHelper.buildProperParameter("markers", "color:white|size:tiny|" + configuration.getGcpDirectionsOriginLat() + "," + configuration.getGcpDirectionsOriginLng()); // Origin
        url += UrlHelper.buildProperParameter("markers", "color:red|size:mid|" + lat + "," + lng); // Destination
        url += hydrantService.generateHydrantsAsMarkers(lat, lng, 100, 2, true, true);

        Optional<String> route = destinationService.getEncodedPolylines(lat, lng);
        if (route.isPresent())
            url += UrlHelper.buildProperParameter("path", "color:0x0000ff60|weight:5|enc:" + route.get());

        return authorizeStaticMapsApiUrl(url);
    }



    private URL authorizeStaticMapsApiUrl(final String url) throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException
    {
        String finalUrl = url + UrlHelper.buildProperParameter("key", configuration.getGcpMapsApiKey());

        URL urlWithoutSignature = new URL(finalUrl);

        if (!configuration.isSigningEnabled())
            return urlWithoutSignature;

        return signer.signUrl(urlWithoutSignature);
    }
}
