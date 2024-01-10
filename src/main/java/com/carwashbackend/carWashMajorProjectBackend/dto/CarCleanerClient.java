package com.carwashbackend.carWashMajorProjectBackend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CarCleanerClient {
    String carNumber;
    String phone;
    String email;
}
