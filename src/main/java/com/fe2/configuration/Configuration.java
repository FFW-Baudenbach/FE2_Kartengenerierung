package com.fe2.configuration;

import com.fe2.helper.FileHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class Configuration implements HealthIndicator {

    @Value("${gcp.maps.apiKey}")
    private String gcp_maps_apiKey;

    @Value("${gcp.maps.signingKey:}")
    private String gcp_maps_signingKey; // Optional

    @Value("${gcp.directions.apiKey:}")
    private String gcp_directions_apiKey; // Optional

    @Value("${gcp.directions.origin.lat:}")
    private String gcp_directions_origin_lat; // Optional

    @Value("${gcp.directions.origin.lng:}")
    private String gcp_directions_origin_lng; // Optional

    @Value("${output.folder:}")
    private String output_folder; // Optional

    @Value("${output.format}")
    private String output_format;

    @Value("${wk.token:}")
    private String wk_token; // Optional

    @Value("#{'${wk.customIcons:}'.split(';')}")
    private List<String> wk_customIcons; // Optional


    public String getGcpMapsApiKey() {
        return gcp_maps_apiKey;
    }

    public String getGcpMapsSigningKey() {
        return gcp_maps_signingKey;
    }

    public String getGcpDirectionsApiKey() {
        return gcp_directions_apiKey;
    }

    public double getGcpDirectionsOriginLat() { return Double.parseDouble(gcp_directions_origin_lat); }

    public double getGcpDirectionsOriginLng() { return Double.parseDouble(gcp_directions_origin_lng); }

    public String getOutputFolder() {
        return output_folder;
    }

    public String getOutputFormat() {
        return output_format;
    }

    public String getWasserkarteInfoToken() {
        return wk_token;
    }

    public Map<Long, String> getConfiguredWasserkarteInfoCustomIcons() {
        var map = new TreeMap<Long, String>();
        wk_customIcons.forEach(l -> {
            if (l == null || l.isBlank())
                return;

            String[] split = l.split("=");
            if (split.length != 2)
                return;

            Long sourceTypeId = Long.parseLong(split[0]);
            String url = split[1];

            map.put(sourceTypeId, url);
        });
        return map;
    }

    public Map<Long, String> getVerifiedWasserkarteInfoCustomIcons()
    {
        var configuredIcons = getConfiguredWasserkarteInfoCustomIcons();
        List<Long> invalidUrls = new ArrayList<>();

        for (Map.Entry<Long, String> entry : configuredIcons.entrySet()) {
            HttpURLConnection huc = null;
            try {
                URL u = new URL ( entry.getValue());

                huc = (HttpURLConnection)u.openConnection();
                huc.setRequestMethod ("GET");
                huc.connect();
                if (huc.getResponseCode() != 200) {
                    throw new IOException("Returned " + huc.getResponseCode());
                }

            }
            catch (Exception e) {
                System.out.println("Configured Icon [" + entry.getKey() + "] does not exist: " + e.getMessage());
                invalidUrls.add(entry.getKey());
            }
            finally {
                if (huc != null)
                    huc.disconnect();
            }
        }

        invalidUrls.forEach(configuredIcons::remove);

        return configuredIcons;
    }

    public boolean isImageStoringEnabled() {
        return output_folder != null && !output_folder.isBlank();
    }

    public boolean isSigningEnabled() {
        return gcp_maps_signingKey != null && !gcp_maps_signingKey.isBlank();
    }

    public boolean isDirectionsApiEnabled() {
        return gcp_directions_apiKey != null && !gcp_directions_apiKey.isBlank() &&
                gcp_directions_origin_lat != null && !gcp_directions_origin_lat.isBlank() &&
                gcp_directions_origin_lng != null && !gcp_directions_origin_lng.isBlank();
    }

    public boolean isWasserkarteInfoApiEnabled() {
        return wk_token != null && !wk_token.isBlank();
    }

    @Override
    public Health health() {

        Map<String, Object> kv = new TreeMap<>();
        boolean isUp = true;

        kv.put("isDirectionsApiEnabled", isDirectionsApiEnabled());
        kv.put("isImageStoringEnabled", isImageStoringEnabled());
        kv.put("isSigningEnabled", isSigningEnabled());
        kv.put("isWasserkarteInfoApiEnabled", isWasserkarteInfoApiEnabled());
        kv.put("WasserkarteInfoCustomIcons-Configured", getConfiguredWasserkarteInfoCustomIcons().size());
        kv.put("WasserkarteInfoCustomIcons-Valid", getVerifiedWasserkarteInfoCustomIcons().size());
        if (isImageStoringEnabled()) {
            Health directoryHealth = FileHelper.canReadWriteDirectory(Paths.get(getOutputFolder()));
            isUp = directoryHealth.getStatus() == Status.UP;

            String directoryMsg = directoryHealth.getDetails().entrySet().iterator().next().getValue().toString();
            kv.put("OutputDirectory-Configured", getOutputFolder());
            kv.put("OutputDirectory-Valid", directoryMsg);
        }
        kv.put("OutputFormat", getOutputFormat());

        if (isUp)
            return Health.up().withDetails(kv).build();

        return Health.down().withDetails(kv).build();
    }
}
