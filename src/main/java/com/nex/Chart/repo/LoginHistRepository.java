package com.nex.Chart.repo;

import com.nex.Chart.entity.LoginHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistRepository extends JpaRepository<LoginHistEntity, Long> {
}
