package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.WashedCarMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface WashedCarMediaRepository extends JpaRepository<WashedCarMedia, Long> {

    WashedCarMedia findBycarNumber(String carNumber);
    List<WashedCarMedia> findByDate(String date);

    String findBycarNumberAndDate(String carNumber, String date);
}
