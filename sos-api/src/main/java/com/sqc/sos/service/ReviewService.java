package com.sqc.sos.service;

import com.sqc.sos.dto.review.ReviewRequest;
import com.sqc.sos.dto.review.ReviewResponse;
import com.sqc.sos.exception.AppException;
import com.sqc.sos.exception.ErrorCode;
import com.sqc.sos.model.Review;
import com.sqc.sos.model.SentimentResult;
import com.sqc.sos.model.CustomerSession;
import com.sqc.sos.model.TableEntity;
import com.sqc.sos.repository.ICustomerSessionRepository;
import com.sqc.sos.repository.IReviewRepository;
import com.sqc.sos.repository.ISentimentResultRepository;
import com.sqc.sos.repository.ITableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final IReviewRepository reviewRepository;
    private final ISentimentResultRepository sentimentResultRepository;
    private final ICustomerSessionRepository customerSessionRepository;
    private final ITableRepository tableRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${ai.service.url:}")
    private String aiServiceUrl;

    public List<ReviewResponse> listAll() {
        return reviewRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        Review review = Review.builder()
                .tableId(request.getTableId())
                .sessionId(request.getSessionId())
                .customerName(resolveCustomerName(request.getCustomerName(), request.getSessionId()))
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);

        SentimentAnalysis analysis = analyzeSentimentWithAi(request.getRating(), request.getComment());
        SentimentResult sentiment = SentimentResult.builder()
                .reviewId(review.getId())
                .sentiment(analysis.sentiment())
                .confidence(analysis.confidence())
                .build();
        sentimentResultRepository.save(sentiment);

        ReviewResponse response = toResponse(review, sentiment);
        eventPublisher.publishEvent(new ReviewCreatedEvent(response));
        return response;
    }

    public Map<String, Long> sentimentSummary() {
        List<SentimentResult> all = sentimentResultRepository.findAll();
        long positive = all.stream().filter(s -> "POSITIVE".equalsIgnoreCase(s.getSentiment())).count();
        long neutral = all.stream().filter(s -> "NEUTRAL".equalsIgnoreCase(s.getSentiment())).count();
        long negative = all.stream().filter(s -> "NEGATIVE".equalsIgnoreCase(s.getSentiment())).count();
        return Map.of("POSITIVE", positive, "NEUTRAL", neutral, "NEGATIVE", negative);
    }

    private ReviewResponse toResponse(Review review) {
        SentimentResult sentiment = sentimentResultRepository.findAll().stream()
                .filter(s -> review.getId().equals(s.getReviewId()))
                .findFirst().orElse(null);
        return toResponse(review, sentiment);
    }

    private ReviewResponse toResponse(Review review, SentimentResult sentiment) {
        return ReviewResponse.builder()
                .id(review.getId())
                .tableId(review.getTableId())
                .tableName(resolveTableName(review.getTableId()))
                .sessionId(review.getSessionId())
                .customerName(review.getCustomerName())
                .rating(review.getRating())
                .comment(review.getComment())
                .sentiment(sentiment != null ? sentiment.getSentiment() : null)
                .sentimentConfidence(sentiment != null ? sentiment.getConfidence() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }

    private String resolveCustomerName(String requestName, String sessionId) {
        if (requestName != null && !requestName.isBlank()) return requestName;
        if (sessionId == null || sessionId.isBlank()) return null;
        return customerSessionRepository.findBySessionId(sessionId)
                .map(CustomerSession::getCustomerName)
                .orElse(null);
    }

    private String resolveTableName(java.util.UUID tableId) {
        if (tableId == null) return null;
        return tableRepository.findById(tableId)
                .map(TableEntity::getName)
                .orElse(null);
    }

    private SentimentAnalysis analyzeSentiment(Integer rating, String comment) {
        String text = comment != null ? comment.toLowerCase(Locale.ROOT) : "";
        int score = rating != null ? rating : 3;
        if (score >= 4 && !containsNegative(text)) {
            return new SentimentAnalysis("POSITIVE", new BigDecimal("0.85"));
        }
        if (score <= 2 || containsNegative(text)) {
            return new SentimentAnalysis("NEGATIVE", new BigDecimal("0.80"));
        }
        return new SentimentAnalysis("NEUTRAL", new BigDecimal("0.70"));
    }

    private SentimentAnalysis analyzeSentimentWithAi(Integer rating, String comment) {
        if (aiServiceUrl != null && !aiServiceUrl.isBlank() && comment != null && !comment.isBlank()) {
            try {
                Map<?, ?> response = WebClient.create(aiServiceUrl)
                        .post()
                        .uri("/sentiment")
                        .bodyValue(Map.of("text", comment))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(Duration.ofSeconds(3));
                if (response != null && response.get("sentiment") != null) {
                    String sentiment = response.get("sentiment").toString();
                    Object confidenceValue = response.get("confidence");
                    BigDecimal confidence = new BigDecimal(String.valueOf(confidenceValue != null ? confidenceValue : "0.75"));
                    return new SentimentAnalysis(sentiment, confidence);
                }
            } catch (Exception ignored) {
            }
        }
        return analyzeSentiment(rating, comment);
    }

    private boolean containsNegative(String text) {
        return text.contains("tệ") || text.contains("dở") || text.contains("lâu")
                || text.contains("chậm") || text.contains("bad") || text.contains("slow");
    }

    private record SentimentAnalysis(String sentiment, BigDecimal confidence) {}
}
