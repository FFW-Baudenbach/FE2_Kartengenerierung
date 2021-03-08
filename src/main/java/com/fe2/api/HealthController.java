package com.fe2.api;

import com.fe2.configuration.Configuration;
import com.fe2.service.HydrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
public class HealthController implements HealthIndicator {

    @Autowired
    public Configuration configuration;

    @Autowired
    public InputController inputController;

    @Autowired
    public HydrantService hydrantService;

    @Value("${debug.lat:49.646412071556114}")
    private double debugLat;

    @Value("${debug.lng:10.564397866729674}")
    private double debugLng;

    @GetMapping("/health")
    public ResponseEntity<String> health(@RequestParam(value = "extended", required = false, defaultValue = "false") boolean extended)
    {
        boolean error = false;

        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<html><head><title>HEALTH</title></head><body><table>");

        addTableLine(htmlTable, true, "isDirectionsApiEnabled", String.valueOf(configuration.isDirectionsApiEnabled()));
        addTableLine(htmlTable, true, "isSigningEnabled", String.valueOf(configuration.isSigningEnabled()));
        addTableLine(htmlTable, true, "isWasserkarteInfoApiEnabled", String.valueOf(configuration.isWasserkarteInfoApiEnabled()));
        addTableLine(htmlTable, true, "WasserkarteInfoCustomIcons-Configured", String.valueOf(configuration.getWasserkarteInfoCustomIcons().size()));
        addTableLine(htmlTable, true, "WasserkarteInfoCustomIcons-Valid", String.valueOf(hydrantService.getVerifiedCustomIcons().size()));
        addTableLine(htmlTable, true, "OutputDirectory-Configured", configuration.getOutputFolder());
        addTableLine(htmlTable, true, "OutputDirectory-Valid", canReadWriteDirectory(Paths.get(configuration.getOutputFolder())).getStatus() == Status.UP ? "true" : "false");
        addTableLine(htmlTable, true, "Output Format", configuration.getOutputFormat());

        if (extended) {
            long start = System.currentTimeMillis();
            var response = inputController.overview(debugLat, debugLng, false);
            long finish = System.currentTimeMillis();
            if (response.getStatusCode() == HttpStatus.OK) {
                addTableLine(htmlTable, false, "<a href=\"/overview?lat=" + debugLat + "&lng=" + debugLng + "\">/overview</a>", (finish-start) + "ms");
            }
            else {
                addTableLine(htmlTable, true, "/overview", response.getBody().toString());
                error = true;
            }

            start = System.currentTimeMillis();
            response = inputController.route(debugLat, debugLng, false);
            finish = System.currentTimeMillis();
            if (response.getStatusCode() == HttpStatus.OK) {
                addTableLine(htmlTable, false, "<a href=\"/route?lat=" + debugLat + "&lng=" + debugLng + "\">/route</a>", (finish - start) + "ms");
            }
            else {
                addTableLine(htmlTable, true, "/route", response.getBody().toString());
                error = true;
            }
        }

        htmlTable.append("</table></body></html>");

        if (error) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlTable.toString());
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlTable.toString());
    }

    private void addTableLine(final StringBuilder sb, boolean escapeHtml, String... columns)
    {
        sb.append("<tr>");
        Arrays.stream(columns).forEach(c -> {
            sb.append("<td>");
            sb.append(escapeHtml ? HtmlUtils.htmlEscape(c) : c);
            sb.append("</td>");
        });
        sb.append("</tr>");
    }

    private Health canReadWriteDirectory(final Path dir) {
        if (!Files.exists(dir))
            return Health.down().withDetail(dir.toString(), "Directory does not exist").build();
        if (!Files.isDirectory(dir))
            return Health.down().withDetail(dir.toString(), "Not a directory").build();
        if (!Files.isReadable(dir))
            return Health.down().withDetail(dir.toString(), "Directory is not readable").build();
        if (!Files.isWritable(dir))
            return Health.down().withDetail(dir.toString(), "Directory is not writable").build();

        return Health.up().withDetail(dir.toString(), "OK").build();
    }

    @Override
    public Health health() {
        return canReadWriteDirectory(Paths.get(configuration.getOutputFolder()));
    }
}
