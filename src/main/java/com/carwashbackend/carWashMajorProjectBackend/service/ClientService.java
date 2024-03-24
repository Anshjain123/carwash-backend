package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.repository.CarJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ClientService {


    @Autowired
    private ClientJPARepository clientJPARepository;

    @Autowired
    private CarJPARepository carJPARepository;

    @Autowired
    private CleanerJPARepository cleanerJPARepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<String> addClient(Client client) {

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
//        oldClient.get().setPlan(client.getPlan());
        oldClient.get().setPassword(passwordEncoder.encode(client.getPassword()));
        oldClient.get().setEmail(client.getEmail());

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

}
