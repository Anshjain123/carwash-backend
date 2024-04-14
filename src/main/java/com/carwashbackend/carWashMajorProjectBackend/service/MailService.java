package com.carwashbackend.carWashMajorProjectBackend.service;

import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.repository.CarJPARepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;



@Service
public class MailService {



//    String message = "hello dear friend";
//    String subject = "mail service";
//    String to = "anshjainalp@gmail.com";
//    String from = "jainansh2510@gmail.com";
//
//    sendEmail(message, subject, to, from);


    @Autowired
    private CarJPARepository carJPARepository;

    @Value("${mailPassword}")
    private String mailPassword;

    public ResponseEntity<Void> sendEmail(String carNumber) {


        System.out.println(carNumber);

        Optional<Car> car = carJPARepository.findById(carNumber);
        if(!car.isPresent()) {
            System.out.println("Yes it is not present");
            throw new BadCredentialsException("not found");
        }
        System.out.println("Yes in mail service it is coming!");
        String to = car.get().getClient().getEmail();
        String from = "majorp1apl@gmail.com";
        String subject = "renew the subscription";
        String planValidity = String.valueOf(car.get().getPlanValidity());
        String message = "Dear customer your car wash plan for carnumber " + carNumber + " is going to expire on " + planValidity + " kindly renew it at earliest to get the best washing services at door step";

        return send(from, to, message, subject);

    }


    public ResponseEntity<Void> send(String from, String to, String message, String subject) {

        // variable for gmail;

        String host = "smtp.gmail.com";
        String port = "587";


        String pass = mailPassword;

        // get the system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES" + properties);

        // setting important information to properties object

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.ssl.enabled", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.user", from);
        properties.put("mail.smtp.password",pass);

        // these properties tell the javamail api where to connect to send the mail

        // step 1 get the session object


        Session instance = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        System.out.println("PRINTING INSTANCE" + instance.getProperties());

        instance.setDebug(true);

        // step 2 -> compose the message [text, multi media...]

        MimeMessage mimeMessage = new MimeMessage(instance);


        try {
            mimeMessage.setFrom(from);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mimeMessage.setSubject(subject);
//            mimeMessage.setText(message);
            mimeMessage.setContent(message, "text/html");
            // step 3 -> send the message

            Transport.send(mimeMessage);
            System.out.println("mail sent sucessfully!");

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

