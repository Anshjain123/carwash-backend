package com.carwashbackend.carWashMajorProjectBackend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Clientotp {

    private String otp;
    @Id
    private String clientUsername;
    private LocalDateTime expiresAt;
}
