package com.scolock.BE_SCOLOCK.domain.user.repository;

import com.scolock.BE_SCOLOCK.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
