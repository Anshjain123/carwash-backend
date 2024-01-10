package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CleanerService {

    @Autowired
    private CleanerJPARepository cleanerJPARepository;

    @Autowired
    private FileService fileService;

    public ResponseEntity<String> addCleaner(Cleaner cleaner, MultipartFile imageData, MultipartFile adhaarData) {

        if(cleanerJPARepository.existsById(cleaner.getEmail())) {
            return new ResponseEntity<>("Error: User with same email already exists", HttpStatus.CONFLICT);
        }

        String imageUri = fileService.getPhotoUrl(imageData);
        String adhaarUri = fileService.getPhotoUrl(adhaarData);

        cleaner.setImageUrl(imageUri);
        cleaner.setAdhaarUrl(adhaarUri);

        cleanerJPARepository.save(cleaner);
        return new ResponseEntity<>("Cleaner added successfully", HttpStatus.CREATED);

    }

    public ResponseEntity<List<Cleaner>> getAllCleaners() {
        try {
            List<Cleaner> cleaners = cleanerJPARepository.findAll();
            return new ResponseEntity<List<Cleaner>>(cleaners, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<String> deleteByemail(String email) {

        try {
            cleanerJPARepository.deleteById(email);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            // Handle the case where the entity with the specified ID was not found
            return new ResponseEntity<>("Error: User not found", HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            // Handle the case where there are still references to the user (foreign key constraint)
            return new ResponseEntity<>("Error: User cannot be deleted due to existing references", HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Handle other unexpected exceptions
            return new ResponseEntity<>("Error: Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
//
    public ResponseEntity<Cleaner> updateCleaner(Cleaner cleaner, String email) {

        if(!cleanerJPARepository.existsById(email)) {
            return new ResponseEntity<>(cleaner, HttpStatus.NOT_FOUND);
        }
        Optional<Cleaner> oldCleaner = cleanerJPARepository.findById(email);
        String imageUrl = oldCleaner.get().getImageUrl();
        String adhaarUrl = oldCleaner.get().getAdhaarUrl();

        try {
            deleteByemail(oldCleaner.get().getEmail());
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(cleaner, HttpStatus.CONFLICT);
        }
        catch (DataAccessException e) {
            return new ResponseEntity<>(cleaner, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        Cleaner newCleaner = new Cleaner();
        newCleaner.setAdhaarUrl(adhaarUrl);
        newCleaner.setImageUrl(imageUrl);

        newCleaner.setName(cleaner.getName());
        newCleaner.setDOB(cleaner.getDOB());
        newCleaner.setEmail(cleaner.getEmail());
        newCleaner.setGender(cleaner.getGender());
        newCleaner.setPhone(cleaner.getPhone());
        newCleaner.setCurrAdd(cleaner.getCurrAdd());
        newCleaner.setPermanentAdd(cleaner.getPermanentAdd());

        try {
            cleanerJPARepository.save(newCleaner);
            return new ResponseEntity<>(newCleaner, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(cleaner, HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(cleaner, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
