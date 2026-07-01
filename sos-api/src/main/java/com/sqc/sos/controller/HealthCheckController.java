package com.sqc.sos.controller;

import com.sqc.sos.dto.ApiResponse;
import com.sqc.sos.dto.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthCheckController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        log.info("Health check requested at: {}", LocalDateTime.now());
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "SOS Restaurant Ordering API");
        healthInfo.put("version", "1.0.0");
        
        return ResponseUtils.ok(healthInfo);
    }
    
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        log.debug("Ping request received");
        return ResponseUtils.ok("pong");
    }
    
    @GetMapping("/readiness")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readinessCheck() {
        log.info("Readiness check requested");
        
        Map<String, Object> readinessInfo = new HashMap<>();
        readinessInfo.put("status", "READY");
        readinessInfo.put("database", "UP");
        readinessInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseUtils.ok(readinessInfo);
    }
    
    @GetMapping("/liveness")
    public ResponseEntity<ApiResponse<Map<String, Object>>> livenessCheck() {
        log.info("Liveness check requested");
        
        Map<String, Object> livenessInfo = new HashMap<>();
        livenessInfo.put("status", "ALIVE");
        livenessInfo.put("timestamp", LocalDateTime.now());
        
        return ResponseUtils.ok(livenessInfo);
    }
    
    @GetMapping("/test-logging")
    public ResponseEntity<ApiResponse<String>> testLogging() {
        log.trace("This is a TRACE message");
        log.debug("This is a DEBUG message");
        log.info("This is an INFO message");
        log.warn("This is a WARN message");
        log.error("This is an ERROR message");
        
        return ResponseUtils.ok("Logging test completed. Check logs directory.");
    }
}
