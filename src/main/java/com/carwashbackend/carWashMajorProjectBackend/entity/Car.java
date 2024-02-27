package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("client")
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("cleaner")
    private Cleaner cleaner;
}
