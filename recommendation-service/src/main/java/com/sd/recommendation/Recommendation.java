package com.sd.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;


@Entity
@Table(name = "recommendations")
public class Recommendation {

    @EmbeddedId
    private RecommendationKey id;

    @Column(name = "score", nullable = false)
    private double score;

    public Recommendation() {}

    public Recommendation(int sourceMovieId, int recommendedMovieId, double score) {
        this.id = new RecommendationKey(sourceMovieId, recommendedMovieId);
        this.score = score;
    }


    @Embeddable
    public static class RecommendationKey implements Serializable {
        @Column(name = "source_movie_id")
        public int sourceMovieId;

        @Column(name = "recommended_movie_id")
        public int recommendedMovieId;

        public RecommendationKey() {}

        public RecommendationKey(int sourceMovieId, int recommendedMovieId) {
            this.sourceMovieId = sourceMovieId;
            this.recommendedMovieId = recommendedMovieId;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecommendationKey that)) return false;
            return sourceMovieId == that.sourceMovieId && recommendedMovieId == that.recommendedMovieId;
        }

        @Override public int hashCode() {
            return Objects.hash(sourceMovieId, recommendedMovieId);
        }
    }
}
