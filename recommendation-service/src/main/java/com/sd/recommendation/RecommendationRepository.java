package com.sd.recommendation;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository
        extends JpaRepository<Recommendation, Recommendation.RecommendationKey> {

    @Query("""
          SELECT r.id.recommendedMovieId
          FROM Recommendation r
          WHERE r.id.sourceMovieId = :sourceId
          ORDER BY r.score DESC
          """)
    List<Integer> findRecommendedIds(@Param("sourceId") int sourceId, Limit limit);
}
