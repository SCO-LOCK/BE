package com.scolock.BE_SCOLOCK.domain.analysis.repository;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, Long> {
}
