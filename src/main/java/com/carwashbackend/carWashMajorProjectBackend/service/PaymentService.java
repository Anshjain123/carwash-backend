package com.carwashbackend.carWashMajorProjectBackend.service;


import com.carwashbackend.carWashMajorProjectBackend.entity.Car;
import com.carwashbackend.carWashMajorProjectBackend.entity.Client;
import com.carwashbackend.carWashMajorProjectBackend.entity.Payment;
import com.carwashbackend.carWashMajorProjectBackend.repository.CarJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.ClientJPARepository;
import com.carwashbackend.carWashMajorProjectBackend.repository.PaymentJPARepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONObject;
import com.razorpay.*;


@Service
public class PaymentService {


    @Autowired
    private CarJPARepository carJPARepository;

    @Autowired
    private ClientJPARepository clientJPARepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private PaymentJPARepository paymentJPARepository;

    @Value("${secretKey}")
    private String secretKey;
    public ResponseEntity<Map<String, String>> createPaymentIntent(Map<String, String> data) throws StripeException {

        Stripe.apiKey = secretKey;

        int amount = Integer.parseInt(data.get("amount"))*100;
        String carNumber = data.get("carNumber");



        Optional<Car> car = carJPARepository.findById(carNumber);

        Client client = null;
        if(car.isPresent()) {
            client = car.get().getClient();
        }

        if(client == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String name = client.getName();
        String city = client.getAddress();

        CustomerCreateParams params =
                CustomerCreateParams.builder()
                        .setName(name)
                        .setAddress(
                                CustomerCreateParams.Address.builder()
                                        .setLine1("510 Townsend St")
                                        .setPostalCode("98140")
                                        .setCity(city)
                                        .setState("CA")
                                        .setCountry("US")

                                        .build()
                        )
                        .setBalance(100000L)
                        .build();

        Customer customer = Customer.create(params);
        System.out.println("printing customer " + customer);
        PaymentIntentCreateParams Params =
                PaymentIntentCreateParams.builder()
                        .setAmount((long) amount)
                        .setCurrency("inr")
                        .addPaymentMethodType("card")
                        .setDescription("Software development services")
                        // In the latest version of the API, specifying the `automatic_payment_methods` parameter is optional because Stripe enables its functionality by default.
                        .setCustomer(customer.getId())
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(Params);
//        paymentIntent.setCustomer(customer.getId());
//        CreatePaymentResponse paymentResponse = new CreatePaymentResponse(paymentIntent.getClientSecret());
        System.out.println("payementIntent " + paymentIntent);

        String clientSecret = paymentIntent.getClientSecret();

        Map<String, String> res = new HashMap<>();
        res.put("clientSecret", clientSecret);
        res.put("id", paymentIntent.getId());
        System.out.println("res -> " + res);
//        System.out.println(res.get("clientSecret"));

        addPayment(car, client, paymentIntent, amount);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    private void addPayment(Optional<Car> car, Client client, PaymentIntent paymentIntent, int amount) {
        Payment payment = new Payment();
        payment.setPaymentMethod("card");
        payment.setId(paymentIntent.getId());
        int amt = amount/100;
        payment.setAmount(amt);
        payment.setDate(getDate());
        payment.setStatus("pending");
        if(car.isPresent()) {
            payment.setCar(car.get());
        }
        payment.setClient(client);
        payment.setCar(car.get());

        paymentJPARepository.save(payment);
        List<Payment> allClientPayments = client.getAllClientPayments();
        allClientPayments.add(payment);
        client.setAllClientPayments(allClientPayments);
        clientJPARepository.save(client);

        List<Payment> allCarPayments = car.get().getAllCarPayments();
        allCarPayments.add(payment);

        car.get().setAllCarPayments(allCarPayments);
        carJPARepository.save(car.get());
    }

    public String getDate() {
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        return date;
    }
    public ResponseEntity<String> getOrderId(Map<String, String> data) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_8XfJ4jqHmM3sYe", "3c21MhdieZeF4CdrzmRst83M");

        int amount = Integer.parseInt(data.get("amount"));
        String receipt = UUID.randomUUID().toString();
        System.out.println(amount);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount*100);
        orderRequest.put("currency","INR");
        orderRequest.put("receipt", receipt);
//
//        JSONObject notes = new JSONObject();
//        notes.put("notes_key_1","Tea, Earl Grey, Hot");
//        orderRequest.put("notes",notes);

        Order order = razorpayClient.orders.create(orderRequest);
        System.out.println(order);
        String orderId = order.get("id");
        Map<String, String> res = new HashMap<>();
        res.put("order_id", orderId);
        return new ResponseEntity<>(order.toString(), HttpStatus.CREATED);
    }


    public ResponseEntity<Void> sendMail(String from, String to, String message, String subject) {
        return mailService.send(from, to, message, subject);
    }

    public ResponseEntity<Void> sendNotification(Map<String, String> data) {

        String carNumber = data.get("carNumber");
        String id = data.get("id");

        System.out.println(id);
        Optional<Car> car = carJPARepository.findById(carNumber);
        Client client = null;
        if(car.isPresent()) {
            client = car.get().getClient();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String clientMail = client.getEmail();
        String adminMail = "jainansh2510@gmail.com";

        List<Payment> allClientPayments = client.getAllClientPayments();
        Payment payment = null;

        for(int i = 0; i < allClientPayments.size(); i++) {
            if(Objects.equals(allClientPayments.get(i).getId(), id)) {
                payment = allClientPayments.get(i);
                break;
            }
        }
        if(payment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        System.out.println("Yes it is not null -> " + payment);
        payment.setStatus("success");
        paymentJPARepository.save(payment);

        // increasing the plan validity for the car;
        Date planValidity = car.get().getPlanValidity();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(planValidity);
        calendar.add(Calendar.MONTH, 1);
        Date increasedDate = calendar.getTime();

        car.get().setPlanValidity(increasedDate);
        carJPARepository.save(car.get());


        String from = adminMail;
        String to = clientMail;
        String message = "Thank you for again choding us for you car washing services your payment for " + carNumber + " is completed your now plan for this car will expire on " + increasedDate;
        String subject = "Payment for carwash service succeed";

        return sendMail(from, to, message, subject);

    }
}
