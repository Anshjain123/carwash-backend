package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.config.CustomAuthenticationManager;
import com.carwashbackend.carWashMajorProjectBackend.dto.JwtRequest;
import com.carwashbackend.carWashMajorProjectBackend.dto.JwtResponse;
import com.carwashbackend.carWashMajorProjectBackend.security.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CleanerAuthService {



    @Autowired
    private CustomAuthenticationManager authenticationManager;

    @Autowired
    private CleanerDetailServiceImp cleanerDetailServiceImp;

    @Autowired
    private JwtHelper helper;

    public ResponseEntity<JwtResponse> login(JwtRequest request) {


        System.out.println("printing request in cleaner auth service");
        System.out.println(request);


        String username = request.getUsername();
        String password = request.getPassword();

        System.out.println("printing username");
        System.out.println(username);
        System.out.println("printing password");
        System.out.println(password);

        if (!doAuthenticate(username, password)) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserDetails userDetails = cleanerDetailServiceImp.loadUserByUsername(username);

        String token = helper.generateToken(userDetails, "cleaner");

        JwtResponse response = JwtResponse.builder().jwtToken(token).username(userDetails.getUsername()).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    boolean doAuthenticate(String username, String password) {

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            authenticationToken.setDetails("cleaner");

            if(authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
                return true;
            } else {
                return false;
            }
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials!");
        }
    }
//
//
//
//    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
////            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
////            System.out.println("Printing authentication token");
//        authenticationToken.setDetails("cleaner");
////            System.out.println(authenticationToken);
////            //[Principal=abc@gmail.com, Credentials=[PROTECTED], Authenticated=false, Details=cleaner, Granted Authorities=[]]
////            // credentials mei password store hota h
////        System.out.println("Printing authenticated response from manager");
//        System.out.println(authenticationManager.authenticate(authenticationToken));
//
//
//    UserDetails userDetails = cleanerDetailServiceImp.loadUserByUsername(request.getUsername());
//
//    String token = helper.generateToken(userDetails, "cleaner");
//

}
