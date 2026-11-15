package com.scolock.BE_SCOLOCK.domain.criteria.repository;

import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriteriaVersionRepository extends JpaRepository<CriteriaVersion, Long> {
    Optional<CriteriaVersion> findFirstByProjectIdOrderByVersionNoDesc(Long projectId);
}
