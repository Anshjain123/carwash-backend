package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.entity.Address;
import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Payment;
import com.carwashbackend.carWashMajorProjectBackend.service.CarService;
import com.carwashbackend.carWashMajorProjectBackend.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ClientController {


    @Autowired
    private ClientService clientService;

    @Autowired
    private CarService carService;

    @GetMapping("/client/getAllClientCars/{username}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Car>> getAllClientCars(@PathVariable String username) {
        return clientService.getAllClientCars(username);
    }

    @GetMapping("/client/getClientAddress/{username}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Address>> getClientAddress(@PathVariable String username) {
        return clientService.getClientAddress(username);
    }

    @GetMapping("client/validateToken")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> validateToken() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/client/getUrlsByDateAndCarNumber")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String[]> getUrlByDateAndCarNumber(@RequestBody Map<String, String> data) {
        String carNumber = data.get("carNumber");
        String date = data.get("date");

        System.out.println("printing date in client controller");
        System.out.println(date);
        System.out.println("printing car number in client controller");
        System.out.println(carNumber);

        String newDate = "";
        newDate += date.charAt(8);
        newDate += date.charAt(9);
        newDate += '-';
        newDate += date.charAt(5);
        newDate += date.charAt(6);
        newDate += '-';
        newDate += date.charAt(0);
        newDate += date.charAt(1);
        newDate += date.charAt(2);
        newDate += date.charAt(3);

        System.out.println("newDate " + newDate);

        return this.carService.getAllUrls(carNumber, newDate);
    }



    @PutMapping("/client/changePassword/{username}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> updatePassword(@PathVariable String username, @RequestBody Map<String, String> data) {
        return clientService.changePassword(username, data);
//        System.out.println(address);
//        System.out.println(username);
//        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/client/updateAddress/{username}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> updateAddress(@PathVariable String username, @RequestBody Address address) {
        return clientService.updateAddress(username, address);
//        System.out.println(address);
//        System.out.println(username);
//        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/client/transactionHistory")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Payment>> transactionHistory(@RequestBody Map<String, String> data) {
        return this.clientService.TransactionHistory(data);
    }

    @PostMapping("/client/addRatings")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> addRatings(@RequestBody Map<String, String> data) {
        return this.clientService.addRatings(data);
    }
}
