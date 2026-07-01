package com.sqc.sos.service;

import com.sqc.sos.dto.review.ReviewResponse;

public class ReviewCreatedEvent {
    private final ReviewResponse review;

    public ReviewCreatedEvent(ReviewResponse review) {
        this.review = review;
    }

    public ReviewResponse getReview() {
        return review;
    }
}
