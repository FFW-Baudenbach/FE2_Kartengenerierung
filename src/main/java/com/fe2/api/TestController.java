package com.fe2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;

@RestController
public class TestController {

    @Autowired
    public InputController inputController;

    @Value("${debug.lat:49.646412071556114}")
    private double debugLat;

    @Value("${debug.lng:10.564397866729674}")
    private double debugLng;

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam(value = "store", required = false, defaultValue = "false") boolean store)
    {
        boolean error = false;

        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<html><head><title>TEST</title></head><body><table>");

        long start = System.currentTimeMillis();
        var response = inputController.overview(debugLat, debugLng, store);
        long finish = System.currentTimeMillis();
        if (response.getStatusCode() == HttpStatus.OK) {
            addTableLine(htmlTable, false, "<a href=\"/overview?lat=" + debugLat + "&lng=" + debugLng + "&store=" + store + "\">/overview</a>", (finish-start) + "ms");
        }
        else {
            addTableLine(htmlTable, true, "/overview", response.getBody().toString());
            error = true;
        }

        start = System.currentTimeMillis();
        response = inputController.route(debugLat, debugLng, store);
        finish = System.currentTimeMillis();
        if (response.getStatusCode() == HttpStatus.OK) {
            addTableLine(htmlTable, false, "<a href=\"/route?lat=" + debugLat + "&lng=" + debugLng + "&store=" + store + "\">/route</a>", (finish - start) + "ms");
        }
        else {
            addTableLine(htmlTable, true, "/route", response.getBody().toString());
            error = true;
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
}
