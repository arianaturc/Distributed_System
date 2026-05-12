package com.sd.recommendation;

import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping
public class RecommendationController {

    private final RecommendationRepository repository;
    private final ChaosInjector chaos;

    public RecommendationController(RecommendationRepository repository, ChaosInjector chaos) {
        this.repository = repository;
        this.chaos = chaos;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "chaos", chaos.isEnabled());
    }

    @GetMapping("/recommendations/{movieId}")
    public ResponseEntity<?> getRecommendations(
            @PathVariable int movieId,
            @RequestParam(defaultValue = "5") int limit) {

        if (chaos.shouldFailRequest()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Recommendation service unavailable (chaos)"));
        }

        List<Integer> recs = repository.findRecommendedIds(movieId, Limit.of(limit));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("movieId", movieId);
        body.put("recommendations", recs);
        return ResponseEntity.ok(body);
    }
}
