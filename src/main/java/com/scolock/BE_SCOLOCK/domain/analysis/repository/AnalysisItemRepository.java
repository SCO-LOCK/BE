package com.scolock.BE_SCOLOCK.domain.analysis.repository;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisItemRepository
        extends JpaRepository<AnalysisItem, Long> {

    List<AnalysisItem> findAllByAnalysisRun_IdOrderByItemOrderAsc(
            Long analysisRunId
    );
}