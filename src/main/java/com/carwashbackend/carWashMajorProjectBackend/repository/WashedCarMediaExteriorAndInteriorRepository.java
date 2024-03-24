package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.WashedCarMediaExteriorAndInterior;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WashedCarMediaExteriorAndInteriorRepository extends JpaRepository<WashedCarMediaExteriorAndInterior, String> {

    List<WashedCarMediaExteriorAndInterior> findByDate(String date);

}
