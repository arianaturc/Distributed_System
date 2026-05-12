package com.sd.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final RecommendationRepository repository;

    public DataSeeder(RecommendationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        List<Recommendation> seed = List.of(
                new Recommendation(1, 2, 0.92), new Recommendation(1, 3, 0.87), new Recommendation(1, 4, 0.81),
                new Recommendation(2, 1, 0.90), new Recommendation(2, 5, 0.76), new Recommendation(2, 6, 0.70),
                new Recommendation(3, 1, 0.88), new Recommendation(3, 4, 0.79), new Recommendation(3, 6, 0.72),
                new Recommendation(4, 1, 0.84), new Recommendation(4, 3, 0.78), new Recommendation(4, 5, 0.71),
                new Recommendation(5, 2, 0.80), new Recommendation(5, 6, 0.77), new Recommendation(5, 1, 0.65),
                new Recommendation(6, 5, 0.83), new Recommendation(6, 2, 0.74), new Recommendation(6, 3, 0.68)
        );
        repository.saveAll(seed);
        log.info("Seeded {} recommendation rows", seed.size());
    }
}
