package com.carwashbackend.carWashMajorProjectBackend;

import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@SpringBootApplication
public class CarWashMajorProjectBackendApplication {


	public static void main(String[] args) {

		SpringApplication.run(CarWashMajorProjectBackendApplication.class, args);


	}



}
