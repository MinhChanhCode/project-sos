package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.review.ReviewRequest;
import com.sqc.sos.dto.review.ReviewResponse;
import com.sqc.sos.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.listAll(), "Lấy đánh giá thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.create(request), "Gửi đánh giá thành công"));
    }

    @GetMapping("/sentiment-summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> sentimentSummary() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.sentimentSummary(), "Lấy thống kê cảm xúc thành công"));
    }
}
