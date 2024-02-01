package com.carwashbackend.carWashMajorProjectBackend.config;

import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
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

        if(userType == "cleaner") {
            Optional<Cleaner> cleaner = cleanerJPARepository.findById(username);
            if(cleaner.isPresent()) {
                if(passwordEncoder.matches(authentication.getCredentials().toString(), cleaner.get().getPassword())) {
                    System.out.println("Yes password are same");
                    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                    UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);
//                    newAuthentication.setAuthenticated(true);
//                    authentication.setAuthenticated(true);
//                    System.out.println("Printing authentication token in manager");
//                    System.out.println(newAuthentication);
                    return newAuthentication;
                } else {
                    throw new BadCredentialsException("Wrong password!");
                }
            } else {
                throw new BadCredentialsException("Wrong username");
            }
        }
        return authentication;
    }
}
