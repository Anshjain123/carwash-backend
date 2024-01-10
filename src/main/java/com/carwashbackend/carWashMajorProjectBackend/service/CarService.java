package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.dto.CarCleanerClient;
import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.repository.CarJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {


    @Autowired
    private CarJPARepository carJPARepository;

    @Autowired
    private CleanerJPARepository cleanerJPARepository;

    @Autowired
    private ClientJPARepository clientJPARepository;

    public ResponseEntity<List<Car>> getAllCars() {

        try {
            List<Car> allCars = carJPARepository.findAll();
            return new ResponseEntity<>(allCars, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<String> assignCarToCleaners(CarCleanerClient carCleanerClient) {

        String email = carCleanerClient.getEmail();
        String carNumber = carCleanerClient.getCarNumber();
        String phone = carCleanerClient.getPhone();
        // email -> cleaner;
        // carNumber -> car;
        // phone -> client;

        // one client may have many cars
        // one cleaner may have many cars

        System.out.println(email);
        System.out.println(phone);
        System.out.println(carNumber);

        Optional<Cleaner> cleaner = cleanerJPARepository.findById(email);
        Optional<Car> car = carJPARepository.findById(carNumber);
        Optional<Client> client = Optional.ofNullable(car.get().getClient());


        car.get().setAssigned(true);
        car.get().setCleaner(cleaner.get());
        cleaner.get().getAllCleanerCars().add(car.get());


//        List<Car> allClientCars = client.get().getAllClientCars();
//        for(int i = 0; i < allClientCars.size(); i++) {
//            if(allClientCars.get(i).getCarNumber() == car.get().getCarNumber()) {
//                allClientCars.get(i).setAssigned(true);
//            }
//        }

        cleanerJPARepository.save(cleaner.get());
        clientJPARepository.save(client.get());


        return new ResponseEntity<String>("Added successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> UnassignCarFromCleaners(String carNumber) {
        Optional<Car> car = carJPARepository.findById(carNumber);
        System.out.println("Printing carNumber");
        System.out.println(carNumber);
        if(car.isPresent()) {
            Cleaner cleaner = car.get().getCleaner();
            cleaner.getAllCleanerCars().remove(car.get());
            Client client = car.get().getClient();

            car.get().setCleaner(null);
            car.get().setAssigned(false);

            List<Car> allClientCars = client.getAllClientCars();
            for(int i = 0; i < allClientCars.size(); i++) {
                if(allClientCars.get(i).getCarNumber() == carNumber) {
                    allClientCars.get(i).setAssigned(false);
                }
            }
            carJPARepository.save(car.get());
            cleanerJPARepository.save(cleaner);
            clientJPARepository.save(client);

            return new ResponseEntity<>("Unassigned successfully!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("such car does not exists!", HttpStatus.CONFLICT);
        }
    }
}