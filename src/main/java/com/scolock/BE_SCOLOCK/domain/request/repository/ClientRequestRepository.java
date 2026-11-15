package com.scolock.BE_SCOLOCK.domain.request.repository;

import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRequestRepository
        extends JpaRepository<ClientRequest, Long> {

    Optional<ClientRequest> findByIdAndProject_Id(
            Long id,
            Long projectId
    );
}
