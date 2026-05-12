package com.sd.movie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final MovieRepository repository;

    public DataSeeder(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        List<Movie> seed = List.of(
                new Movie(1, "Inception", "A thief who steals corporate secrets through dream-sharing technology."),
                new Movie(2, "The Matrix", "A hacker discovers reality as he knows it is a simulation."),
                new Movie(3, "Interstellar", "Explorers travel through a wormhole in search of a new home for humanity."),
                new Movie(4, "Tenet", "An operative manipulates the flow of time to prevent World War III."),
                new Movie(5, "Blade Runner 2049", "A young blade runner uncovers a secret that could plunge society into chaos."),
                new Movie(6, "Arrival", "A linguist works with the military to communicate with alien lifeforms.")
        );
        repository.saveAll(seed);
        log.info("Seeded {} movies", seed.size());
    }
}
