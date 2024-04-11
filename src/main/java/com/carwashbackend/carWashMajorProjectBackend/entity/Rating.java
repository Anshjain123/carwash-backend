package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long rating;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("cleanerRatings")
    private Cleaner cleaner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("clientRatings")
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference("carRatings")
    private Car car;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String date;
}
