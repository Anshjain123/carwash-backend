package com.carwashbackend.carWashMajorProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.security.Identity;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class WashedCarMedia {

    @Id
    @GeneratedValue
    private Long id;
    private List<String> uris;

    private String carNumber;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private String date;
}
