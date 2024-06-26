package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.dto.CarCleanerClient;
import com.carwashbackend.carWashMajorProjectBackend.dto.ResponseCar;
import com.carwashbackend.carWashMajorProjectBackend.entity.*;
import com.carwashbackend.carWashMajorProjectBackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    private  WashedCarMediaRepository washedCarMediaRepository;

    @Autowired
    private WashedCarMediaExteriorAndInteriorRepository washedCarMediaExteriorAndInteriorRepository;

    public List<Car> getAllCars() {

        try {
            List<Car> allCars = carJPARepository.findAll();
//            System.out.println("yes it is coming!");
            return allCars;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, String> getAllCarsAssignedCleaners() {
        List<Car> allCars = getAllCars();

        HashMap<String, String> res = new HashMap<>();

        for(int i = 0; i < allCars.size(); i++) {
            System.out.println("Yes inside for loop it is coming!");
            String key = allCars.get(i).getCarNumber();
//            String value = allCars.get(i).getCleaner().getEmail();
            Cleaner cleaner = allCars.get(i).getCleaner();
            if(cleaner != null) {
                String value = cleaner.getEmail();
                res.put(key, value);
            }
//            String value = cleaner.getEmail()
//            res.put(key, value);
        }

        return res;
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

//        kafkaService.notifyCleaner(email);



//        simpMessagingTemplate.convertAndSend("topic/userUpdates", email);

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

    public ResponseEntity<String[]> getAllUrls(String carNumber, String date) {

        try {
//            String URI = washedCarMediaRepository.findBycarNumberAndDate(carNumber, date);
            List<WashedCarMedia> washedCars = washedCarMediaRepository.findByDate(date);
//            System.out.println(washedCars);
            String URI = "";
            for(int i = 0; i < washedCars.size(); i++) {
                if(washedCars.get(i).getCarNumber().equals(carNumber)) {
                    URI = washedCars.get(i).getURI();
                    break;
                }
            }
            String[] uris = URI.split(",");
            System.out.println(uris);

            List<WashedCarMediaExteriorAndInterior> washedCarMediaExteriorAndInteriors = washedCarMediaExteriorAndInteriorRepository.findByDate(date);

            if(URI.equals("")) {
                String ExtURI = "";
                String IntURI = "";
                for(int i = 0; i < washedCarMediaExteriorAndInteriors.size(); i++) {
                    if(washedCarMediaExteriorAndInteriors.get(i).getCarNumber().equals(carNumber)) {
                        ExtURI = washedCarMediaExteriorAndInteriors.get(i).getExtURI();
                        IntURI = washedCarMediaExteriorAndInteriors.get(i).getIntURI();
                        break;
                    }
                }
                String[] Exturis = ExtURI.split(",");
                String[] Inturis = IntURI.split(",");


                if(ExtURI.equals("") && IntURI.equals("")) {
                    String[] res = new String[0];
                    return new ResponseEntity<>(res, HttpStatus.OK);
                }
                int len = Exturis.length + Inturis.length;

                System.out.println(len);

                String[] res = new String[len];

                int i = 0, j = 0, k = 0;

                while(i < Exturis.length) {
                    res[k] = Exturis[i];
                    k++;
                    i++;
                }
                while(j < Inturis.length) {
                    res[k] = Inturis[j];
                    k++;
                    j++;
                }

                uris = res;
                System.out.println("printing ext and int uris");
                System.out.println(uris);
            }

            System.out.println("Yes comming herrrrree!");
            return new ResponseEntity<>(uris, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
