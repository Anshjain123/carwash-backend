package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class FileController {


    @Autowired
    private FileService fileService;

    @GetMapping("/getImage/{filename}")
    @CrossOrigin(origins = "*")
    public @ResponseBody  byte[] getImage(@PathVariable String filename) throws IOException {
        return fileService.getFile(filename);
    }



}
