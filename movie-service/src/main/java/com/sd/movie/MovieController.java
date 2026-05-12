package com.sd.movie;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class MovieController {

    private final MovieRepository movies;
    private final RecommendationClient recClient;
    private final CircuitBreakerRegistry breakerRegistry;

    public MovieController(MovieRepository movies,
                           RecommendationClient recClient,
                           CircuitBreakerRegistry breakerRegistry) {
        this.movies = movies;
        this.recClient = recClient;
        this.breakerRegistry = breakerRegistry;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/circuit")
    public Map<String, Object> circuit() {
        CircuitBreaker cb = breakerRegistry.circuitBreaker("recommendationService");
        var m = cb.getMetrics();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", cb.getName());
        body.put("state", cb.getState().toString());
        body.put("failureRate", m.getFailureRate());
        body.put("slowCallRate", m.getSlowCallRate());
        body.put("bufferedCalls", m.getNumberOfBufferedCalls());
        body.put("failedCalls", m.getNumberOfFailedCalls());
        body.put("slowCalls", m.getNumberOfSlowCalls());
        body.put("successfulCalls", m.getNumberOfSuccessfulCalls());
        body.put("notPermittedCalls", m.getNumberOfNotPermittedCalls());
        return body;
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<?> getMovie(@PathVariable int id) {
        Optional<Movie> movieOpt = movies.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Movie not found"));
        }

        RecommendationClient.Result result = recClient.getRecommendations(id, 5);
        List<Movie> recommended = movies.findByIdIn(result.ids());

        recommended.sort((a, b) -> Integer.compare(result.ids().indexOf(a.getId()),
                                                   result.ids().indexOf(b.getId())));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("movie", movieOpt.get());
        body.put("recommendations", recommended);
        body.put("degraded", result.fallback());
        body.put("reason", result.reason());
        return ResponseEntity.ok(body);
    }
}
