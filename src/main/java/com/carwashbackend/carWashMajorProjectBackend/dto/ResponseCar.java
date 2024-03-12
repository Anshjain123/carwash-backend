package com.carwashbackend.carWashMajorProjectBackend.dto;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Getter
@Setter
public class ResponseCar {

    private String carModel;
    @Id
    private String carNumber;
    private String description;
    private String cleanerId;
}
