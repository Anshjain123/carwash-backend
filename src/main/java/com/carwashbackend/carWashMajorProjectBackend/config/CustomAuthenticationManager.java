package com.carwashbackend.carWashMajorProjectBackend.config;

import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomAuthenticationManager implements AuthenticationManager {

    @Autowired
    private ClientJPARepository clientJPARepository;

    @Autowired
    private CleanerJPARepository cleanerJPARepository;


    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userType = (String) authentication.getDetails();
        String username = authentication.getName();
//        System.out.println("Printing userType in customAuthentication manager");
//        System.out.println(userType);
//        System.out.println("Printing username in customeAuthentication manager");
//        System.out.println(username);
//        System.out.println("Printing credentials in customAutgentication manager");
//        System.out.println(authentication.getCredentials());

        if(userType.equals("cleaner")) {
            Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);
            System.out.println("debug");
            if(cleaner.isPresent()) {
                boolean flag = passwordEncoder.matches(authentication.getCredentials().toString(), cleaner.get().getPassword());
                System.out.println(flag);
                if(flag) {
                    System.out.println("Yes password are same");
                    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                    UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);
//
                    return newAuthentication;
                } else {
                    System.out.println("wrong");
                    throw new BadCredentialsException("Wrong password!");
                }
            } else {
                throw new BadCredentialsException("Wrong username");
            }
        } else {
            Optional<Client> client = clientJPARepository.findById(username);
            if(client.isPresent()) {
//                System.out.println(client.get().getPassword());
                boolean flag = passwordEncoder.matches(authentication.getCredentials().toString(), client.get().getPassword());
                System.out.println(authentication.getCredentials().toString());
                System.out.println(flag);
                if(flag == true) {
//                    System.out.println("debug client");
                    System.out.println("Yes password are same!");
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);

                    return newAuthentication;
                } else {
                    throw new BadCredentialsException("Wrong password!");
                }
            } else {
                throw new BadCredentialsException("Wrong username");
            }
        }
    }
}
