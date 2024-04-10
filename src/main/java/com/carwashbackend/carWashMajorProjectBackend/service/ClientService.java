package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.entity.*;
import com.carwashbackend.carWashMajorProjectBackend.repository.CarJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private PasswordEncoder passwordEncoder;

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

                clientJPARepository.save(client.get());
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



}
