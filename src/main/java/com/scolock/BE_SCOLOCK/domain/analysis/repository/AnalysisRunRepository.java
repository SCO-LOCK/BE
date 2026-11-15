package com.scolock.BE_SCOLOCK.domain.analysis.repository;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, Long> {

    Optional<AnalysisRun> findByIdAndClientRequest_Id(
            Long id,
            Long requestId
    );
}