package com.carwashbackend.carWashMajorProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cleaner {

    @Id
    @Column(name = "email", unique = true)
    private String email;
    private String name;
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date DOB;

    private String currAdd;
    private String permanentAdd;


    private String phone;
    private String gender;
    private String ImageUrl;
    private String adhaarUrl;
    private long totalRatings;
    private long totalRaters;

    @OneToMany(mappedBy = "cleaner", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference("cleaner")
    private List<Car> allCleanerCars;

    @OneToMany(mappedBy = "cleaner", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference("cleanerRatings")
    private List<Rating> allCleanerRatings;
}
