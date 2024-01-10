package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    @JsonBackReference("client")
    private Client client;

    @ManyToOne
    @JsonBackReference("cleaner")
    private Cleaner cleaner;
}