package com.scolock.BE_SCOLOCK.domain.request.repository;

import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClientRequest> findByIdAndProjectId(Long id, Long projectId);
}
