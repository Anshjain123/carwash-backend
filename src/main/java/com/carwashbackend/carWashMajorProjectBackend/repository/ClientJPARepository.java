package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientJPARepository extends JpaRepository<Client, String> {

    Optional<Client> findById(String phone);
    Optional<Client> findByemail(String email);

}
