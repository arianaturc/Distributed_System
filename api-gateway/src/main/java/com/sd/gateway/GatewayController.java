package com.sd.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final RestClient restClient;
    private final String movieServiceUrl;

    public GatewayController(RestClient restClient,
                             @Value("${movie.service.url}") String movieServiceUrl) {
        this.restClient = restClient;
        this.movieServiceUrl = movieServiceUrl;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "gateway", true);
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<?> movie(@PathVariable int id) {
        return proxy(movieServiceUrl + "/movies/" + id);
    }

    @GetMapping("/circuit")
    public ResponseEntity<?> circuit() {
        return proxy(movieServiceUrl + "/circuit");
    }

    private ResponseEntity<?> proxy(String url) {
        try {
            JsonNode body = restClient.get().uri(url).retrieve().body(JsonNode.class);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("Upstream unreachable: {} ({})", url, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Upstream service unavailable", "upstream", url));
        }
    }
}
