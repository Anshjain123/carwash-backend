package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.service.CleanerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class CleanerController {



    @Autowired
    private CleanerService cleanerService;

    @GetMapping("/cleaner/getAllCleanerCars/{username}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<Car>> getAllCleanerCars(@PathVariable String username) {
        return cleanerService.getAllCleanerCars(username);
    }

    @PostMapping("/cleaner/postMedia")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> postMedia(@RequestBody Map<Object, Object> mp) throws IOException {
//        System.out.println(li);

        List<String> li = (List<String>) mp.get("allImagesData");
        String carNumber = (String) mp.get("carNumber");
        cleanerService.addWashedCarMedia(li, carNumber);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/cleaner/postMediaExtAndInt")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> postMediaExtAndInt(@RequestBody Map<Object, Object> mp) throws IOException {
//        System.out.println(li);

        List<String> ExtFiles = (List<String>) mp.get("extFiles");
        List<String> IntFiles = (List<String>) mp.get("intFiles");
        String carNumber = (String) mp.get("carNumber");
        cleanerService.addWashedCarMedia(ExtFiles, IntFiles, carNumber);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/getMedia/{filename}")
    @CrossOrigin(origins = "*")
    public @ResponseBody  byte[] getMedia(@PathVariable String filename) throws IOException {
        System.out.println("printing the filename");
        System.out.println(filename);
        return cleanerService.getMedia(filename);
    }


    @GetMapping("/getCarWashedToday")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<String>> getCarWashedToday() throws ParseException {
//        List<String> cars = this.cleanerService.getAllWashedCarToday();
//        return new ResponseEntity<>(cars, HttpStatus.OK);
        return cleanerService.getAllWashedCarToday();
    }




}
