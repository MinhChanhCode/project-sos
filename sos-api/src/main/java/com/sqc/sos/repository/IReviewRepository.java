package com.sqc.sos.repository;

import com.sqc.sos.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
}
