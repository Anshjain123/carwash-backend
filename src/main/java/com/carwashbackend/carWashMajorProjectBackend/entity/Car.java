package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

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

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date planValidity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("client")
    private Client client;

    @JsonBackReference("cleaner")
    @ManyToOne(fetch = FetchType.EAGER)
    private Cleaner cleaner;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference("carPayment")
    List<Payment> allCarPayments;

}
