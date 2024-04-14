package com.carwashbackend.carWashMajorProjectBackend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cleanerotp {

    private String otp;

    @Id
    private String cleanerUsername;

    private LocalDateTime expiresAt;
}
