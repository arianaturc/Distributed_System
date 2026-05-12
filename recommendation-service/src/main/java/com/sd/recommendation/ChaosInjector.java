package com.sd.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;


@Component
public class ChaosInjector {

    private static final Logger log = LoggerFactory.getLogger(ChaosInjector.class);

    private static final double FAILURE_PROBABILITY = 0.30;
    private static final double LATENCY_PROBABILITY = 0.40;
    private static final int MIN_LATENCY_MS = 3000;
    private static final int MAX_LATENCY_MS = 10000;

    public boolean isEnabled() {
        String v = System.getenv("CHAOS_MODE");
        return v != null && v.equalsIgnoreCase("true");
    }

    public boolean shouldFailRequest() {
        if (!isEnabled()) return false;

        ThreadLocalRandom tlr = ThreadLocalRandom.current();


        if (tlr.nextDouble() < LATENCY_PROBABILITY) {
            int delay = tlr.nextInt(MIN_LATENCY_MS, MAX_LATENCY_MS + 1);
            log.warn("[CHAOS] Injecting {} ms of latency", delay);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (tlr.nextDouble() < FAILURE_PROBABILITY) {
            log.warn("[CHAOS] Injecting 503 failure");
            return true;
        }
        return false;
    }
}
