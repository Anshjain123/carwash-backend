package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingJPARepository extends JpaRepository<Rating, Long> {
}
