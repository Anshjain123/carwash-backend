package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarJPARepository extends JpaRepository<Car, String> {

    List<Car> findByClient(Client client);
    void deleteByClient(Client client);
    void deleteById(String carNumber);

    Optional<Car> findById(String carNumber);

}
