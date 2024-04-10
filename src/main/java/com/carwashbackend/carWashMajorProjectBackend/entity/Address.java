package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Address {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String addressLine;
    private String pincode;
    private String city;
    private String state;

    @ManyToOne
    @JsonBackReference("clientAddresses")
    private Client client;


}
