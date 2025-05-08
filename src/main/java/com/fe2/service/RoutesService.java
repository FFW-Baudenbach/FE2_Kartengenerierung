package com.fe2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fe2.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class RoutesService {

    @Autowired
    private Configuration configuration;


    public Optional<String> getEncodedPolylines(double destLat, double destLng)
    {
        if (!configuration.isRoutesApiEnabled())
            return Optional.empty();

        try {
            String endpoint = "https://routes.googleapis.com/directions/v2:computeRoutes?key=" + configuration.getGcpRoutesApiKey();

            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-Goog-FieldMask", "routes.polyline.encodedPolyline");
            conn.setDoOutput(true);

            String jsonBody = String.format(Locale.US, """
                {
                  "origin": {
                    "location": {
                      "latLng": {
                        "latitude": %f,
                        "longitude": %f
                      }
                    }
                  },
                  "destination": {
                    "location": {
                      "latLng": {
                        "latitude": %f,
                        "longitude": %f
                      }
                    }
                  },
                  "travelMode": "DRIVE"
                }
                """, configuration.getGcpRoutesOriginLat(), configuration.getGcpRoutesOriginLng(), destLat, destLng);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("API call failed with status code: " + status);
                return Optional.empty();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            conn.disconnect();

            String json = response.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(json);
            JsonNode polylineNode = root.path("routes").path(0).path("polyline").path("encodedPolyline");

            if (!polylineNode.isMissingNode() && !polylineNode.isNull()) {
                return Optional.of(polylineNode.asText());
            }
            else {
                return Optional.empty();
            }

        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

}
