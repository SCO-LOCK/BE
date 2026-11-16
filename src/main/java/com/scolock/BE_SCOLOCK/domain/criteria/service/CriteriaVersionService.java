package com.scolock.BE_SCOLOCK.domain.criteria.service;

import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.criteria.repository.CriteriaVersionRepository;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CriteriaVersionService {

    private final CriteriaVersionRepository criteriaVersionRepository;

    public CriteriaVersion getLatestCriteriaVersionByProjectId(Long projectId) {
        return criteriaVersionRepository.findFirstByProjectIdOrderByVersionNoDesc(projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CRITERIA_VERSION_NOT_FOUND));
    }
}
