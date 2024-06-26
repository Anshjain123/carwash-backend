package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.entity.*;
import com.carwashbackend.carWashMajorProjectBackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class ClientService {


    @Autowired
    private ClientJPARepository clientJPARepository;

    @Autowired
    private CarJPARepository carJPARepository;

    @Autowired
    private CleanerJPARepository cleanerJPARepository;


    @Autowired
    private RatingJPARepository ratingJPARepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientotpJPARepository clientotpJPARepository;

    @Autowired
    private MailService mailService;

    public ResponseEntity<String> addClient(Client client) throws ParseException {

        List<Car> allCars = client.getAllClientCars();

        for(int i = 0; i < allCars.size(); i++) {
            allCars.get(i).setClient(client);
        }

//        Optional<Client> oldClient = clientJPARepository.findById(client.getPhone());
//        System.out.println(oldClient.get().getName());
        if(clientJPARepository.existsById(client.getPhone())) {
            // here existsById is better instead of findbyId because findById will retreieve the client while
            // existsById will not retrieve it when i use findByid it retrieves the entity and detaches it with the
            // database so when we save it (reaatach it) it might cause error in case of multiple managedReferences and
            // backreferences of same entity

            return new ResponseEntity<String>("client with same phone number already exists", HttpStatus.CONFLICT);
        }

        client.setPassword(passwordEncoder.encode(client.getPassword()));

        Date date = client.getAllClientCars().get(0).getPlanValidity();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        String formattedDate = dateFormat.format(date);
        date = dateFormat.parse(formattedDate);

        client.getAllClientCars().get(0).setPlanValidity(date);
        System.out.println("printing validityplan date");
        System.out.println(client.getAllClientCars().get(0).getPlanValidity());
        clientJPARepository.save(client);
        return new ResponseEntity<String>("Client is added successfully", HttpStatus.CREATED);
    }

    public List<Client> getAllClients() {
        try {

            List<Client> allClients = clientJPARepository.findAll();
            System.out.println(allClients);
            return allClients;
        } catch (Error e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public ResponseEntity<String> deleteByid(String carNumber) {

        Optional<Car> car = carJPARepository.findById(carNumber);
        if(car.isPresent()) {
//            System.out.println("YEs here we are comming heenifonw");
            Client client = car.get().getClient();
            List<Car> clientCars = client.getAllClientCars();
            clientCars.remove(car.get());
            client.setAllClientCars(clientCars);
            client.getAllClientCars().remove(car.get());

            List<Payment> allClientPayments = car.get().getAllCarPayments();
            for(int i = 0; i < allClientPayments.size(); i++) {
                allClientPayments.get(i).setClient(null);
            }
            List<Rating> allCarRatings = car.get().getAllCarRatings();
            for(int i = 0; i < allCarRatings.size(); i++) {
                allCarRatings.get(i).setClient(null);
            }

            Cleaner cleaner = car.get().getCleaner();
            if(cleaner != null) {
                System.out.println("Printing car of cleaner");
                System.out.println(cleaner.getAllCleanerCars().size());
                cleaner.getAllCleanerCars().remove(car.get());
                cleanerJPARepository.save(cleaner);
                System.out.println("Printing car of cleaner after deletion");
                System.out.println(cleaner.getAllCleanerCars().size());
            }


            carJPARepository.deleteById(carNumber);
            List<Car> allCars = carJPARepository.findByClient(client);

            System.out.println("Printing all cars size");
            System.out.println(allCars.size());
//            System.out.println(allCars);

            if(allCars.size() == 0) {
                clientJPARepository.deleteById(client.getPhone());
            } else {
                clientJPARepository.save(client);
            }


            return new ResponseEntity<>("Deleted sucessful", HttpStatus.OK);
        }
        return new ResponseEntity<>("Cannot delete the car since no client is present related to this car", HttpStatus.CONFLICT);

    }
//
    public ResponseEntity<Client> updateClient(Client client) {

        System.out.println("printing client");
        System.out.println(client.getName());

        Optional<Client> oldClient = clientJPARepository.findById(client.getPhone());

        oldClient.get().setName(client.getName());
        oldClient.get().setAddress(client.getAddress());
        oldClient.get().setAge(client.getAge());
        oldClient.get().setGender(client.getGender());
        oldClient.get().setAllClientAddresses(client.getAllClientAddresses());
//        oldClient.get().setPlan(client.getPlan());
//        System.out.println("printing client password " + client.getPassword());
//        oldClient.get().setPassword(client.getPassword());
        oldClient.get().setEmail(client.getEmail());
        oldClient.get().setAllClientPayments(client.getAllClientPayments());
        oldClient.get().setAllClientRatings(client.getAllClientRatings());

        List<Car> allCars = client.getAllClientCars();

        for(int i = 0; i < allCars.size(); i++) {
            Optional<Car> car = carJPARepository.findById(allCars.get(i).getCarNumber());
            if(car.isPresent()) {
                car.get().setCarModel(allCars.get(i).getCarModel());
//                car.get().setAssigned(allCars.get(i).isAssigned());
                car.get().setDescription(allCars.get(i).getDescription());
            } else {
                oldClient.get().getAllClientCars().add(allCars.get(i));
                allCars.get(i).setClient(oldClient.get());
            }
        }
        clientJPARepository.save(oldClient.get());

        Optional<Client> testClient = clientJPARepository.findById(client.getPhone());

        System.out.println("Printing updated client");
        System.out.println(testClient.get().getName());

        return new ResponseEntity<>(oldClient.get(), HttpStatus.CREATED);
    }


    public ResponseEntity<List<Car>> getAllClientCars(String username) {

        Optional<Client> client = clientJPARepository.findById(username);

        if(client.isPresent()) {
            try {
                List<Car> allClientCars = client.get().getAllClientCars();

                return new ResponseEntity<>(allClientCars, HttpStatus.OK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            throw new BadCredentialsException("No such client exists!");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<List<Address>> getClientAddress(String username) {
        Optional<Client> client = clientJPARepository.findById(username);

        if(client.isPresent()) {

            try {
                List<Address> allClientAddresses = client.get().getAllClientAddresses();
                return new ResponseEntity<>(allClientAddresses, HttpStatus.FOUND);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else {
            throw new BadCredentialsException("No such client exists!");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<Void> updateAddress(String username, Address address) {
        Optional<Client> client = clientJPARepository.findById(username);

        if(client.isPresent()) {

            try {
                List<Address> allClientAddresses = client.get().getAllClientAddresses();
                allClientAddresses.get(0).setAddressLine(address.getAddressLine());
                allClientAddresses.get(0).setCity(address.getCity());
                allClientAddresses.get(0).setState(address.getState());
                allClientAddresses.get(0).setPincode(address.getPincode());

                try {

                    clientJPARepository.save(client.get());

                    String from = "majorp1apl@gmail.com";
                    String to = "jainansh2510@gmail.com";
                    String subject = "Address changed by client " + client.get().getName();
                    String message = "<html><body>" +
                            "<h1 style='color: #5e9ca0;'>Hello Admin,</h1>" +
                            "<p style='color: #5e9ca0;'>A Client has recently changed his address.</p>" +
                            "<p style='color: #5e9ca0;'>Here's Client information:</p>" +
                            "<p>" + "client name -> " + client.get().getName() + "</p>" +
                            "<p>" + "client phone -> " + client.get().getPhone() + "</p>" +
                            "<p style='color: #5e9ca0;'>If you think this was a mistake kindly contact client as soon as possible</p>" +
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


    public ResponseEntity<Void> changePassword(String username, Map<String, String> data) {
        String newPassword = data.get("newPassword");
        String oldPassword = data.get("oldPassword");
        Optional<Client> client = clientJPARepository.findById(username);

        if(client.isPresent()) {

            try {

                if(passwordEncoder.matches(oldPassword, client.get().getPassword())) {
                    System.out.println("Yes they same!");
                    client.get().setPassword(passwordEncoder.encode(newPassword));
                    clientJPARepository.save(client.get());
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


    public ResponseEntity<List<Payment>> TransactionHistory(Map<String, String> data) {

        String carNumber = data.get("carNumber");
        String username = data.get("username");

        Optional<Client> client = clientJPARepository.findById(username);
        if(client.isPresent()) {

            try {
                List<Payment> allClientPayments = client.get().getAllClientPayments();
                List<Payment> res = new ArrayList<>();

                for(int i = 0; i < allClientPayments.size(); i++) {
                    if(allClientPayments.get(i).getCar().getCarNumber().equals(carNumber)) {
                        res.add(allClientPayments.get(i));
                    }
                }

                return new ResponseEntity<>(res, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public String getDate() {
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        return date;
    }
    public ResponseEntity<String> addRatings(Map<String, String> data) {
        try {
            System.out.println("Yes we are coming here in add Ratings");
            String carNumber = data.get("carNumber");
            // fetching client
            String username = data.get("username");
            Optional<Car> car = carJPARepository.findById(carNumber);
            if(car.isPresent()) {
                Cleaner cleaner = car.get().getCleaner();
                if(cleaner == null) {
                    return new ResponseEntity<>("Cleaner is null", HttpStatus.NOT_FOUND);
                }
                Optional<Client> client = clientJPARepository.findById(username);
                long rating = Long.parseLong(data.get("rating"));

                cleaner.setTotalRatings(cleaner.getTotalRatings() + rating);
                cleaner.setTotalRaters(cleaner.getTotalRaters() + 1);


                String date = getDate();

                Rating rating1 = new Rating();
                rating1.setRating(rating);
                rating1.setDate(date);
                rating1.setCleaner(cleaner);
                rating1.setClient(client.get());
                rating1.setCar(car.get());
                ratingJPARepository.save(rating1);

                List<Rating> allCleanerRatings = cleaner.getAllCleanerRatings();
                allCleanerRatings.add(rating1);

                List<Rating> allClientRatings = client.get().getAllClientRatings();
                allClientRatings.add(rating1);

                List<Rating> allCarRatings = car.get().getAllCarRatings();
                allCarRatings.add(rating1);

                clientJPARepository.save(client.get());
                cleanerJPARepository.save(cleaner);
                carJPARepository.save(car.get());
                return new ResponseEntity<>("rating added successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public ResponseEntity<Void> isUserValid(String email) {
//        System.out.println("Yes is uservalid " + username);

        Optional<Client> client = clientJPARepository.findByemail(email);
//        System.out.println("yes it is valid");

        if(client.isPresent()) {
            String username = client.get().getPhone();
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
                + "<p>To complete your verification process, please use the following OTP:</p>"
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
        Optional<Client> client= clientJPARepository.findById(username);

        if(client.isPresent()) {
            String otp = generateOtp();
            LocalDateTime expiryTime = calculateExpiryTime();

            System.out.println(client.get().getEmail());
            String from = "majorp1apl@gmail.com";
            String to = client.get().getEmail();
            String subject = "Otp for password change ";
            String message = composeOtpMessage(client.get().getEmail(), otp);
            mailService.send(from, to, message, subject);

            // store the otp
            Optional<Clientotp> clientotp = clientotpJPARepository.findById(client.get().getEmail());
//            Cleanerotp cleanerotp = cleanerotpJPARepository.findBycleanerUsername(cleaner.get().getEmail());
            if(!clientotp.isPresent()) {
                Clientotp clientotp1 = new Clientotp();
                clientotp1.setClientUsername(username);
                clientotp1.setOtp(otp);
                clientotp1.setExpiresAt(expiryTime);
                clientotpJPARepository.save(clientotp1);

            } else {
                clientotp.get().setExpiresAt(expiryTime);
                clientotp.get().setOtp(otp);
                clientotpJPARepository.save(clientotp.get());
            }
        }
    }

    public ResponseEntity<String> isValidOtp(Map<String, String> data) {
        String email = data.get("username");
        Optional<Client> client = clientJPARepository.findByemail(email);

        if(client.isPresent()) {
            String username = client.get().getPhone();
            Optional<Clientotp> clientotp = clientotpJPARepository.findById(username);
//        System.out.println(username);
            if(clientotp.isPresent()) {

                String userOtp = data.get("otp");
                if(userOtp.equals(clientotp.get().getOtp())) {

                    LocalDateTime nowTime = LocalDateTime.now();
                    Duration duration = Duration.between(clientotp.get().getExpiresAt(), nowTime);

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

        }


        return new ResponseEntity<>("No such user exists", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> changePasswordLogin(Map<String, String> data) {

        String username = data.get("username");
        Optional<Client> client = clientJPARepository.findByemail(username);
        if(client.isPresent()) {
            try {

                String newPassword = data.get("newPassword");
                client.get().setPassword(passwordEncoder.encode(newPassword));
                clientJPARepository.save(client.get());
                return new ResponseEntity<>("Password changed succesfully", HttpStatus.OK);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>("User not present", HttpStatus.NOT_FOUND);
    }
}
