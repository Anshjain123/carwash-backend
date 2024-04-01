package com.carwashbackend.carWashMajorProjectBackend.controller;


import com.carwashbackend.carWashMajorProjectBackend.service.PaymentService;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@RestController
public class PaymentController {

//    Stripe.apiKey = "pk_test_51OzADOSAbmkAKQN9dP7LTny9r3uqq7sZYRwJbCB4DDTgF0qtklqM4sJLckY313JlzSZYnr6rekjJS8rTS7XGXcCg00TBi8zpxF";
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/client/createorder")
//    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createOrder(@RequestBody Map<String, String> data) throws RazorpayException {
        return paymentService.getOrderId(data);
    }

    @PostMapping("/client/createpaymentintent")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, String> data) throws StripeException {
        return paymentService.createPaymentIntent(data);
    }


    @PostMapping("/client/sendPaymentSuccessNotification")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> sendPaymentSuccessNotification(@RequestBody Map<String, String> data) throws StripeException {
        return paymentService.sendNotification(data);
    }
}


