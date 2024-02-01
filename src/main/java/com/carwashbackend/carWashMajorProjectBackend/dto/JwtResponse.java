package com.carwashbackend.carWashMajorProjectBackend.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class JwtResponse {

    private String jwtToken;
    private String username;
}
