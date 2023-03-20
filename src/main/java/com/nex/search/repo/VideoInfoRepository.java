package com.nex.search.repo;

import com.nex.search.entity.VideoInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoInfoRepository extends JpaRepository<VideoInfoEntity, Long> {
    List<VideoInfoEntity> findAllByTsiUno(Integer tsiUno);
}
