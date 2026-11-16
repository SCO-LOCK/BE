package com.scolock.BE_SCOLOCK.domain.analysis.repository;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, Long> {
    boolean existsByClientRequestIdAndStatusIn(Long requestId, Collection<AnalysisStatus> statuses);
}
