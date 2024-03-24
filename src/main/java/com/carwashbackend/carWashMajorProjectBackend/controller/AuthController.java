package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.dto.JwtRequest;
import com.carwashbackend.carWashMajorProjectBackend.dto.JwtResponse;
import com.carwashbackend.carWashMajorProjectBackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {


    @Autowired
    private AuthService authService;

    @PostMapping("/login/cleaner")
    @CrossOrigin(origins = "*")
    public ResponseEntity<JwtResponse> cleanerLogin(@RequestBody JwtRequest request) {
        return authService.login(request);
    }

    @PostMapping("/login/client")
    @CrossOrigin(origins = "*")
    public ResponseEntity<JwtResponse> clientLogin(@RequestBody JwtRequest request) {
        return authService.login(request);
    }

}
