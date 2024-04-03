package com.carwashbackend.carWashMajorProjectBackend.repository;

import com.carwashbackend.carWashMajorProjectBackend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressJPARespository extends JpaRepository<Address, Long> {
}
