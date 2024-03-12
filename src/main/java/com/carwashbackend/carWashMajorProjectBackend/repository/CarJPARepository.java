package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarJPARepository extends JpaRepository<Car, String> {

    List<Car> findByClient(Client client);
    void deleteByClient(Client client);

}
