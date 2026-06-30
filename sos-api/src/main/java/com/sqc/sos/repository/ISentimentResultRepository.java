package com.sqc.sos.repository;

import com.sqc.sos.model.SentimentResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISentimentResultRepository extends JpaRepository<SentimentResult, Long> {
}
