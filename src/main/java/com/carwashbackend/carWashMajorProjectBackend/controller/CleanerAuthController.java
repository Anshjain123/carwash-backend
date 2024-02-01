package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.dto.JwtRequest;
import com.carwashbackend.carWashMajorProjectBackend.dto.JwtResponse;
import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.service.CleanerAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CleanerAuthController {


    @Autowired
    private CleanerAuthService cleanerAuthService;

    @PostMapping("/login/cleaner")
    @CrossOrigin(origins = "*")
    public ResponseEntity<JwtResponse> getAllCleanerCars(@RequestBody JwtRequest request) {
        return cleanerAuthService.login(request);
    }

}
