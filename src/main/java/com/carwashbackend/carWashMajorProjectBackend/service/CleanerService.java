package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.entity.*;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
//import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerotpJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerotpJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.WashedCarMediaExteriorAndInteriorRepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.WashedCarMediaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
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


    @Autowired
    private MailService mailService;

//    @Autowired
//    private CleanerotpJPARepository cleanerotpJPARepository;

    @Autowired
    private CleanerotpJPARepository cleanerotpJPARepository;
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
        cleaner.setTotalRaters(0);
        cleaner.setTotalRatings(0);
        cleanerJPARepository.save(cleaner);
        return new ResponseEntity<>("Cleaner added successfully", HttpStatus.CREATED);

    }

    public ResponseEntity<List<Cleaner>> getAllCleaners() {
        try {
            List<Cleaner> cleaners = cleanerJPARepository.findAll();
            System.out.println("printing all cleaners");
//            System.out.println(cleaners);
            return new ResponseEntity<List<Cleaner>>(cleaners, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<String> deleteByemail(String email) {

        try {
            Optional<Cleaner> cleaner = cleanerJPARepository.findById(email);
            if(cleaner.isPresent()) {

                List<Rating> allCleanerRatings = cleaner.get().getAllCleanerRatings();
                List<Car> allCleanerCars = cleaner.get().getAllCleanerCars();

                for(int i = 0; i < allCleanerRatings.size(); i++) {
                    allCleanerRatings.get(i).setCleaner(null);
                }
                for(int i = 0; i < allCleanerCars.size(); i++) {
                    allCleanerCars.get(i).setAssigned(false);
                    allCleanerCars.get(i).setCleaner(null);
                }

                cleanerJPARepository.deleteById(email);
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            }
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
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
        newCleaner.setTotalRatings(cleaner.getTotalRatings());
        newCleaner.setTotalRaters(cleaner.getTotalRaters());
        newCleaner.setAllCleanerRatings(cleaner.getAllCleanerRatings());
//        newCleaner.setPassword(passwordEncoder.encode(cleaner.getPassword()));

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





    public String getURI(List<String> files, String carNumber, int cnt) throws IOException {
        int n = files.size();


        String date = getDate();

        String directory = "uploadMedia/" + date + "/" + carNumber;

        Path storageDirectory = Paths.get(directory);

        if(!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }

        List<String> uris = new ArrayList<>();
        String URI = "";

        cnt = 11;
        for(int i = 0; i < n; i++) {
            String uniqueId = UUID.randomUUID().toString();
            String fileName = "image" + uniqueId + '@' + date + '$' + carNumber;
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


    public String getPlateNumber(String base64Image) throws JsonProcessingException {
        Map<String, String> body = new HashMap<>();
        body.put("upload", base64Image);
//        body.put("regions", "us-ca");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Token cd3f07703b175ef9a69e27ba587d1c8752d7a8e3");

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.platerecognizer.com/v1/plate-reader/", requestEntity, String.class);

        String responseBody = response.getBody();

//        System.out.println(responseBody);
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        var results = jsonNode.get("results");
        if(response.hasBody() && results.size() > 0) {

            try {
                String plateNumber = results.get(0).get("plate").toString();
                return plateNumber;
            } catch (RuntimeException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";

    }

    public boolean checkCarNumber(List<String> files, String carNumber) throws JsonProcessingException {

        String lowerCaseCarNumber = carNumber.toLowerCase();
        // trying to apply the dip model

        for(int i = 0; i < files.size(); i++) {

            String res = getPlateNumber(files.get(i)).toLowerCase();
            if(res.length() > 0) {

                String plateNumber = res.substring(1, res.length()-1);
                System.out.println(i);
                System.out.println(plateNumber);
                System.out.println(lowerCaseCarNumber);
                if(plateNumber.equals(lowerCaseCarNumber)) {
                    return true;
                }
            }
        }
        return false;

    }
    public ResponseEntity<String> addWashedCarMediaExt(List<String> files, String carNumber) throws IOException {

        if(!checkCarNumber(files, carNumber)) {
            return new ResponseEntity<>("Cannot upload files because carNumber plate is not correct", HttpStatus.CONFLICT);
        }


        String URI = getURI(files, carNumber, 0);
        String date = getDate();
        WashedCarMedia washedCarMedia = new WashedCarMedia();
        washedCarMedia.setURI(URI);
        washedCarMedia.setDate(date);
        washedCarMedia.setCarNumber(carNumber);
        washedCarMediaRepository.save(washedCarMedia);

//        WashedCarMedia washedCarMedia1 = washedCarMediaRepository.findBycarNumber(carNumber);
//
//        System.out.println(washedCarMedia1);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<String> addWashedCarMedia(List<String> ExtFiles, List<String> IntFiles, String carNumber) throws IOException {

        if(!checkCarNumber(ExtFiles, carNumber)) {
            return new ResponseEntity<>("Cannot upload files because carNumber plate is not correct", HttpStatus.CONFLICT);
        }

        String ExtURI = getURI(ExtFiles, carNumber, 0);
        String IntURI = getURI(IntFiles, carNumber, 5);
        String date = getDate();

        WashedCarMediaExteriorAndInterior washedCarMediaExteriorAndInterior = new WashedCarMediaExteriorAndInterior();

        washedCarMediaExteriorAndInterior.setExtURI(ExtURI);
        washedCarMediaExteriorAndInterior.setIntURI(IntURI);
        washedCarMediaExteriorAndInterior.setDate(date);
        washedCarMediaExteriorAndInterior.setCarNumber(carNumber);

        washedCarMediaExteriorAndInteriorRepository.save(washedCarMediaExteriorAndInterior);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public byte[] getMedia(String filename) throws IOException {
        int startIdx = 0;
        int endIdx = 0;
        for(int i = startIdx; i < filename.length(); i++) {
            if(filename.charAt(i) == '@') {
                startIdx = i+1;
//                break;
            } else if(filename.charAt(i) == '$'){
                endIdx = i+1;
                break;
            }
        }
        System.out.println(filename);
        System.out.println("startIdx");
        System.out.println(startIdx);
        String carNumber = filename.substring(endIdx);
        String date = filename.substring(startIdx, endIdx-1);

        String directory = "uploadMedia/" + date + "/" + carNumber;
//        String directory = "uploadMedia/" + ;
        System.out.println(directory);
        Path storageDirectory = Path.of(directory);
        if(!Files.exists(storageDirectory)) {
            return null;
        }
        System.out.println("Yes dir present!");

        Path destination = Path.of(storageDirectory + "\\" + filename);
        byte[] data = Files.readAllBytes(destination);
        System.out.println("data");
        System.out.println(data);
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

    public ResponseEntity<Void> updateAddress(Map<String, String> data) {
        String username = data.get("username");
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);

        if(cleaner.isPresent()) {

            try {

                String newCurrentAddress = data.get("currentAddress");
                String newPermanentAddress = data.get("permanentAddress");

                cleaner.get().setCurrAdd(newCurrentAddress);
                cleaner.get().setPermanentAdd(newPermanentAddress);

                try {

                    cleanerJPARepository.save(cleaner.get());
                    String from = "majorp1apl@gmail.com";
                    String to = "jainansh2510@gmail.com";
                    String subject = "Address changed by cleaner " + cleaner.get().getName();
                    String message = "<html><body>" +
                            "<h1 style='color: #5e9ca0;'>Hello Admin,</h1>" +
                            "<p style='color: #5e9ca0;'>A Cleaner has recently changed his address.</p>" +
                            "<p style='color: #5e9ca0;'>Here's Cleaner information:</p>" +
                            "<p>" + "cleaner name -> " + cleaner.get().getName() + "</p>" +
                            "<p>" + "cleaner phone -> " + cleaner.get().getPhone() + "</p>" +
                            "<p style='color: #5e9ca0;'>If you think this was mistake kindly contact cleaner as soon as possible</p>" +
                            "<p style='color: #5e9ca0;'>Thank you for your attention.</p>" +
                            "<p style='color: #5e9ca0;'>Best regards,<br>Washify team</p>" +
                            "</body></html>";
                    mailService.send(from, to, message, subject);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }



                return new ResponseEntity<>(HttpStatus.OK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else {
            throw new BadCredentialsException("No such client exists!");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<Void> changePassword(Map<String, String> data) {
//        System.out.println("Yes password coming change");
        String newPassword = data.get("newPassword");
        String oldPassword = data.get("oldPassword");
        String username = data.get("username");
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);

        if(cleaner.isPresent()) {

            try {
//                System.out.println("Yes comint here");
                System.out.println(oldPassword);
                if(passwordEncoder.matches(oldPassword, cleaner.get().getPassword())) {
                    System.out.println("Yes they same!");
                    cleaner.get().setPassword(passwordEncoder.encode(newPassword));
                    cleanerJPARepository.save(cleaner.get());
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }

            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else {
            throw new BadCredentialsException("No such client exists!");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<Map<String, String>> getCleanerAddress(String username) {
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);

        if(cleaner.isPresent()) {

            try {
                String currAdd = cleaner.get().getCurrAdd();
                String permanentAdd = cleaner.get().getPermanentAdd();

                Map<String, String> res = new HashMap<>();
                res.put("currAdd", currAdd);
                res.put("permanentAdd", permanentAdd);

                System.out.println(res);
                return new ResponseEntity<>(res, HttpStatus.OK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else {
            throw new BadCredentialsException("No such client exists!");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // forget password

    public ResponseEntity<Void> isUserValid(String username) {
//        System.out.println("Yes is uservalid " + username);

        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);
//        System.out.println("yes it is valid");

        if(cleaner.isPresent()) {
            sendOtp(username);
            return new ResponseEntity<>(HttpStatus.FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public static String generateOtp() {
        // Generate a random 5-digit OTP
        Random random = new Random();
        int otp = 10000 + random.nextInt(90000); // Generates a random number between 10000 and 99999
        return String.valueOf(otp);
    }

    public static LocalDateTime calculateExpiryTime() {
        // Calculate expiry time as current time + 5 minutes
        return LocalDateTime.now().plusMinutes(5);
    }

    private String composeOtpMessage(String user, String otp) {
        return "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>OTP Email</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f5f5f5; color: #333; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #fff; border-radius: 5px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + "h1 { color: #007bff; }"
                + "p { font-size: 16px; line-height: 1.5; }"
                + ".otp { font-size: 24px; font-weight: bold; color: #28a745; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<h1>Verify Your Account</h1>"
                + "<p>Hello " + user + ",</p>"
                + "<p>Thank you for using our service! To complete your verification process, please use the following OTP:</p>"
                + "<p class=\"otp\">" + otp + "</p>"
                + "<p>This OTP is valid for 5 minutes only. Please do not share it with anyone for security reasons.</p>"
                + "<p>If you did not request this OTP, please ignore this message.</p>"
                + "<p>Best regards,<br>Washify</p>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public void sendOtp(String username) {

        System.out.println("uyes yaahan bhi valid h" + username);
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);

        if(cleaner.isPresent()) {
            String otp = generateOtp();
            LocalDateTime expiryTime = calculateExpiryTime();

            System.out.println(cleaner.get().getEmail());
            String from = "majorp1apl@gmail.com";
            String to = cleaner.get().getEmail();
            String subject = "Otp for password change ";
            String message = composeOtpMessage(cleaner.get().getEmail(), otp);
            mailService.send(from, to, message, subject);

            // store the otp
            Optional<Cleanerotp> cleanerotp = cleanerotpJPARepository.findById(cleaner.get().getEmail());
//            Cleanerotp cleanerotp = cleanerotpJPARepository.findBycleanerUsername(cleaner.get().getEmail());
            if(!cleanerotp.isPresent()) {
                Cleanerotp cleanerotp1 = new Cleanerotp();
                cleanerotp1.setCleanerUsername(username);
                cleanerotp1.setOtp(otp);
                cleanerotp1.setExpiresAt(expiryTime);
                cleanerotpJPARepository.save(cleanerotp1);

            } else {
                cleanerotp.get().setExpiresAt(expiryTime);
                cleanerotp.get().setOtp(otp);
                cleanerotpJPARepository.save(cleanerotp.get());
            }
//            cleanerotp.setOtp(otp);
//            cleanerotp.setExpiredAt(expiryTime);
        }
    }

    public ResponseEntity<String> isValidOtp(Map<String, String> data) {
        String username = data.get("username");
        Optional<Cleanerotp> cleanerotp = cleanerotpJPARepository.findById(username);
        if(cleanerotp.isPresent()) {

            String userOtp = data.get("otp");
            if(userOtp.equals(cleanerotp.get().getOtp())) {

                LocalDateTime nowTime = LocalDateTime.now();
                Duration duration = Duration.between(cleanerotp.get().getExpiresAt(), nowTime);

                if (duration.getSeconds() <= 5 * 60) {

                    System.out.println("The difference is not more than 5 minutes.");

                    return new ResponseEntity<>("OTP is valid", HttpStatus.OK);
                } else {
                    System.out.println("The difference is more than 5 minutes.");
                    return new ResponseEntity<>("OTP is expired", HttpStatus.CONFLICT);
                }
            }


            return new ResponseEntity<>("Wrong Otp", HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>("No such user exists", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> changePasswordLogin(Map<String, String> data) {

        String username = data.get("username");
        Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);
        if(cleaner.isPresent()) {
            try {

                String newPassword = data.get("newPassword");
                cleaner.get().setPassword(passwordEncoder.encode(newPassword));
                cleanerJPARepository.save(cleaner.get());
                return new ResponseEntity<>("Password changed succesfully", HttpStatus.OK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>("User not present", HttpStatus.NOT_FOUND);
    }
}
