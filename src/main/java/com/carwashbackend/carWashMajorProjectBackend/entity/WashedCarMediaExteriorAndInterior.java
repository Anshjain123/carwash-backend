package com.carwashbackend.carWashMajorProjectBackend.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WashedCarMediaExteriorAndInterior {

    @Id
    private String carNumber;


    @Column(length = 500)
    private String ExtURI;
    @Column(length = 500)
    private String IntURI;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String date;
    
}
