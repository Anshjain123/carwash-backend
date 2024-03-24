package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Car {

    private String carModel;
    @Id
    private String carNumber;
    private String description;
    private boolean assigned;
    private String plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("client")
    private Client client;

    @JsonBackReference("cleaner")
    @ManyToOne(fetch = FetchType.EAGER)
    private Cleaner cleaner;
}
