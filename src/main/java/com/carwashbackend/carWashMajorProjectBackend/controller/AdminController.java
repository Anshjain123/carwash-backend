package com.carwashbackend.carWashMajorProjectBackend.controller;

import com.carwashbackend.carWashMajorProjectBackend.dto.CarCleanerClient;
import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.service.CarService;
import com.carwashbackend.carWashMajorProjectBackend.service.CleanerService;
import com.carwashbackend.carWashMajorProjectBackend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5005")
@RequestMapping("/admin")
public class AdminController {



    @Autowired
    private CleanerService cleanerService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private CarService carService;

    @PostMapping("/cleaner/add")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<String> addCleaner(@RequestPart("imageData") MultipartFile imageData, @RequestPart("adhaarData") MultipartFile adhaarData, @RequestPart("cleaner") Cleaner cleaner) {
        cleanerService.addCleaner(cleaner, imageData, adhaarData);
        return new ResponseEntity<>("It is fine", HttpStatus.CREATED);
    }

    @GetMapping("/cleaner/getAll")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Cleaner>> getAllCleaners() {
        return cleanerService.getAllCleaners();
    }

    @DeleteMapping("/cleaner/delete/{email}")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<String> deleteCleaner(@PathVariable String email) {
        return cleanerService.deleteByemail(email);
    }
////
    @PutMapping("/cleaner/update/{email}")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<Cleaner> updateCleaner(@RequestBody Cleaner cleaner, @PathVariable String email) {
        return cleanerService.updateCleaner(cleaner, email);
    }

    @PostMapping("/client/add")
    public ResponseEntity<String> addClient(@RequestBody Client client) {
        return clientService.addClient(client);
    }

    @GetMapping("/client/getAll")
    @CrossOrigin(origins = "http://localhost:5005")
    public @ResponseBody List<Client> getAllClients() {

        List<Client> allClients = clientService.getAllClients();
        return allClients;
    }

    @DeleteMapping("/client/delete/{carNumber}")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<String> deleteclientCar(@PathVariable String carNumber) {
        return clientService.deleteByid(carNumber);
    }

    @PutMapping("/client/update")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<Client> deleteclientCar(@RequestBody Client client) {
        return clientService.updateClient(client);
    }

    @GetMapping("/assignCars/getAll")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<List<Car>> getAllCars() {
        return carService.getAllCars();
    }

    @PostMapping("/assignCars/assignCarToCleaners")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<String> assignCarToCleaners(@RequestBody CarCleanerClient carCleanerClient) {
        System.out.println("Printing carCleanerClient");
        System.out.println(carCleanerClient);
        return carService.assignCarToCleaners(carCleanerClient);
    }


    @PostMapping("/assignCars/unassign")
    @CrossOrigin(origins = "http://localhost:5005")
    public ResponseEntity<String> UnassignCarFromCleaners(@RequestBody Map<String, String> jsonData) {

        System.out.println("Printing jsonData");
        System.out.println(jsonData);

        String carNumber = jsonData.get("carNumber");
        return carService.UnassignCarFromCleaners(carNumber);
    }

}
