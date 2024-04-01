package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Payment {

    @Id
    private String id;

    @ManyToOne
    @JsonBackReference("clientPayment")
    private Client client;

    @ManyToOne
    @JsonBackReference("carPayment")
    private Car car;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String date;

    private int amount;
    private String status;
    private String paymentMethod;

}
