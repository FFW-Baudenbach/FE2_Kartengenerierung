package com.fe2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class Configuration {

    @Value("${gcp.apiKey}")
    private String gcp_apiKey;

    @Value("${gcp.signingKey:}")
    private String gcp_signingKey; // Optional

    @Value("${overview.outputFile}")
    private String overview_outputFile;

    @Value("${wk.token:}")
    private String wk_token; // Optional

    @Value("#{'${wk.customIcons:}'.split(';')}")
    private List<String> wk_customIcons; // Optional

    public String getGcpApiKey() {
        return gcp_apiKey;
    }

    public String getGcpSigningKey() {
        return gcp_signingKey;
    }

    public String getOverviewOutputFile() {
        return overview_outputFile;
    }

    public String getWasserkarteInfoToken() {
        return wk_token;
    }

    public Map<Long, String> getWasserkarteInfoCustomIcons() {
        var map = new TreeMap<Long, String>();
        wk_customIcons.forEach(l -> {
            String[] split = l.split("=");
            map.put(Long.parseLong(split[0]), split[1]);
        });
        return map;
    }
}
