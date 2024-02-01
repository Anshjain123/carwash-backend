package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.dto.CustomCleanerDetails;
import com.carwashbackend.carWashMajorProjectBackend.dto.CustomClientDetails;
import com.carwashbackend.carWashMajorProjectBackend.entity.Cleaner;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.repository.CleanerJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientDetailServiceImp implements UserDetailsService {


    @Autowired
    private ClientJPARepository clientJPARepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Client> client = clientJPARepository.findById(username);

        return new CustomClientDetails(client.get());
    }
}
