package com.nex.user.repo;

import com.nex.user.entity.LoginHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistRepository extends JpaRepository<LoginHistEntity, Long> {
}
