package com.sd.movie;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationClient {

    private static final Logger log = LoggerFactory.getLogger(RecommendationClient.class);

    private static final List<Integer> TRENDING_IDS = List.of(1, 2, 3);

    private final RestClient restClient;
    private final String baseUrl;

    public RecommendationClient(RestClient restClient,
                                @Value("${recommendation.service.url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }


    @CircuitBreaker(name = "recommendationService", fallbackMethod = "fallback")
    public Result getRecommendations(int movieId, int limit) {
        String url = baseUrl + "/recommendations/" + movieId + "?limit=" + limit;
        JsonNode body = restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);

        List<Integer> ids = new ArrayList<>();
        if (body != null && body.has("recommendations") && body.get("recommendations").isArray()) {
            body.get("recommendations").forEach(n -> ids.add(n.asInt()));
        }
        return new Result(ids, false, "ok");
    }


    @SuppressWarnings("unused")
    Result fallback(int movieId, int limit, Throwable t) {
        String reason = classify(t);
        log.info("[FALLBACK] movieId={} reason={} ({})", movieId, reason, t.getMessage());
        return new Result(TRENDING_IDS, true, reason);
    }

    private String classify(Throwable t) {
        if (t instanceof CallNotPermittedException) return "circuit-open";
        if (t instanceof ResourceAccessException)  return "timeout";
        if (t instanceof RestClientResponseException r) return "upstream-" + r.getStatusCode().value();
        return "error";
    }

    public record Result(List<Integer> ids, boolean fallback, String reason) {}
}
