package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.WashedCarMedia;
import com.carwashbackend.carWashMajorProjectBackend.entity.WashedCarMediaExteriorAndInterior;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.WashedCarMediaExteriorAndInteriorRepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.WashedCarMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.swing.text.html.Option;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CleanerService {

    @Autowired
    private CleanerJPARepository cleanerJPARepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WashedCarMediaRepository washedCarMediaRepository;

    @Autowired
    private WashedCarMediaExteriorAndInteriorRepository washedCarMediaExteriorAndInteriorRepository;


    public int cnt = 0;
    public ResponseEntity<String> addCleaner(Cleaner cleaner, MultipartFile imageData, MultipartFile adhaarData) {

        if(cleanerJPARepository.existsById(cleaner.getEmail())) {
            return new ResponseEntity<>("Error: User with same email already exists", HttpStatus.CONFLICT);
        }

        String imageUri = fileService.getPhotoUrl(imageData);
        String adhaarUri = fileService.getPhotoUrl(adhaarData);

        cleaner.setImageUrl(imageUri);
        cleaner.setAdhaarUrl(adhaarUri);
        cleaner.setPassword(passwordEncoder.encode(cleaner.getPassword()));

        cleanerJPARepository.save(cleaner);
        return new ResponseEntity<>("Cleaner added successfully", HttpStatus.CREATED);

    }

    public ResponseEntity<List<Cleaner>> getAllCleaners() {
        try {
            List<Cleaner> cleaners = cleanerJPARepository.findAll();
            System.out.println("printing all cleaners");
            System.out.println(cleaners);
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
        newCleaner.setPassword(passwordEncoder.encode(cleaner.getPassword()));

        try {
            cleanerJPARepository.save(newCleaner);
            return new ResponseEntity<>(newCleaner, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(cleaner, HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(cleaner, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public ResponseEntity<List<Car>> getAllCleanerCars(String username) {

        System.out.println("Printing username in getAllCleanerCars in cleaner Service");
        System.out.println(username);
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);
        if(cleaner.isPresent()) {
            try {
                List<Car> allCleanerCars = cleaner.get().getAllCleanerCars();
                return new ResponseEntity<List<Car>>(allCleanerCars, HttpStatus.OK);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public String getURI(List<String> files, String carNumber) throws IOException {
        int n = files.size();

        String date = getDate();

        String directory = "uploadMedia/" + date + "/" + carNumber;

        Path storageDirectory = Paths.get(directory);

        if(!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }

        List<String> uris = new ArrayList<>();
        String URI = "";


        for(int i = 0; i < n; i++) {
            String fileName = "image" + cnt + carNumber;
            cnt++;
            String destination = storageDirectory + "\\" + fileName;
            byte[] imageBytes = Base64.getDecoder().decode(files.get(i));
            Files.copy(new ByteArrayInputStream(imageBytes), Path.of(destination));

            String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("getMedia/")
                    .path(fileName)
                    .toUriString();

            System.out.println("printing image uri in cleaner service ");
            System.out.println(uri);
            uris.add(uri);
            URI += uri;
            if(i < n-1) {
                URI += ',';
            }
            System.out.println(uri);
        }

        return URI;
    }

    public String getDate() {
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        return date;
    }
    public void addWashedCarMedia(List<String> files, String carNumber) throws IOException {


        String URI = getURI(files, carNumber);
        String date = getDate();
        WashedCarMedia washedCarMedia = new WashedCarMedia();
        washedCarMedia.setURI(URI);
        washedCarMedia.setDate(date);
        washedCarMedia.setCarNumber(carNumber);
        washedCarMediaRepository.save(washedCarMedia);

//        WashedCarMedia washedCarMedia1 = washedCarMediaRepository.findBycarNumber(carNumber);
//
//        System.out.println(washedCarMedia1);
    }

    public void addWashedCarMedia(List<String> ExtFiles, List<String> IntFiles, String carNumber) throws IOException {

        String ExtURI = getURI(ExtFiles, carNumber);
        String IntURI = getURI(IntFiles, carNumber);
        String date = getDate();

        WashedCarMediaExteriorAndInterior washedCarMediaExteriorAndInterior = new WashedCarMediaExteriorAndInterior();

        washedCarMediaExteriorAndInterior.setExtURI(ExtURI);
        washedCarMediaExteriorAndInterior.setIntURI(IntURI);
        washedCarMediaExteriorAndInterior.setDate(date);
        washedCarMediaExteriorAndInterior.setCarNumber(carNumber);

        washedCarMediaExteriorAndInteriorRepository.save(washedCarMediaExteriorAndInterior);

    }

    public byte[] getMedia(String filename) throws IOException {
        String carNumber = filename.substring(6);
        String date = getDate();

        String directory = "uploadMedia/" + date + "/" + carNumber;
//        String directory = "uploadMedia/" + ;
        Path storageDirectory = Path.of(directory);
        if(!Files.exists(storageDirectory)) {
            return null;
        }

        Path destination = Path.of(storageDirectory + "\\" + filename);
        byte[] data = Files.readAllBytes(destination);
//        System.out.println(data);
        return data;
    }

    public ResponseEntity<List<String>> getAllWashedCarToday() {
        String date = getDate();

        try {

            System.out.println(date);
            List<WashedCarMedia> washedCarMedia = washedCarMediaRepository.findAll();
            List<WashedCarMediaExteriorAndInterior> washedCarMediaExteriorAndInteriors = washedCarMediaExteriorAndInteriorRepository.findAll();

            System.out.println(washedCarMedia);

            List<String> res = new ArrayList<>();

            for(int i = 0; i < washedCarMedia.size(); i++) {
                String datei = washedCarMedia.get(i).getDate();
                if(datei.equals(date)) {
                    res.add(washedCarMedia.get(i).getCarNumber());
                }
            }

            for(int i = 0; i < washedCarMediaExteriorAndInteriors.size(); i++) {
                String datei = washedCarMediaExteriorAndInteriors.get(i).getDate();
                if(datei.equals(date)) {
                    res.add(washedCarMediaExteriorAndInteriors.get(i).getCarNumber());
                }
            }


            return new ResponseEntity<List<String>>(res, HttpStatus.OK);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


}
