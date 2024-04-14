package com.carwashbackend.carWashMajorProjectBackend.security;

import com.carwashbackend.carWashMajorProjectBackend.service.CleanerDetailServiceImp;
import com.carwashbackend.carWashMajorProjectBackend.service.ClientDetailServiceImp;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

@Component
public class Jwtfilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);
    @Autowired
    private JwtHelper jwtHelper;


    @Autowired
    private CleanerDetailServiceImp cleanerDetailServiceImp;

    @Autowired
    private ClientDetailServiceImp clientDetailServiceImp;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        //Authorization

        System.out.println(request.getRequestURI());

        if(!(request.getRequestURI().startsWith("/admin") || request.getRequestURI().startsWith("/login") || request.getRequestURI().startsWith("/getImage") || request.getRequestURI().startsWith("/getPdf") || request.getRequestURI().startsWith("/getMedia"))){


//            System.out.println(request.getRequestURI());

            System.out.println("jwt filter mei aarha h!");
            System.out.println(request.getRequestURI());

            String requestHeader = request.getHeader("Authorization");

            //Bearer 2352345235sdfrsfgsdfsdf
            logger.info(" Header :  {}", requestHeader);
            String username = null;
            String token = null;

            if (requestHeader != null && requestHeader.startsWith("Bearer")) {
                //looking good
                token = requestHeader.substring(7);
                try {

                    username = this.jwtHelper.getUsernameFromToken(token);

                } catch (IllegalArgumentException e) {
                    logger.info("Illegal Argument while fetching the username !!");
                    e.printStackTrace();
                } catch (ExpiredJwtException e) {
                    logger.info("Given jwt token is expired !!");
                    e.printStackTrace();
                } catch (MalformedJwtException e) {
                    logger.info("Some changed has done in token !! Invalid Token");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                logger.info("Invalid Header Value !! ");
                throw new RuntimeException("Authorisation header not present");
            }


            // trying to authenticate

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {


                // fetch the userType

                String userType = this.jwtHelper.getUserTypeFromToken(token);

                System.out.println("Printign the userType");
                System.out.println(userType);
                //fetch user detail from username
                System.out.println("printing the username in jwtFilter");
                System.out.println(username);
                UserDetails userDetails;
                System.out.println("Printing the type of cleaner");

//            if(userType.equals("cleaner")) {
//                System.out.println("Yes it is same userType that i have sent");
//            }

                if(userType.equals("cleaner")) {
                    System.out.println("Yes the usertype is cleaner");
                    userDetails = this.cleanerDetailServiceImp.loadUserByUsername(username);
                } else {
                    userDetails = this.clientDetailServiceImp.loadUserByUsername(username);
                }

                System.out.println("Printing the userDetails in JwtFilter");
                System.out.println(userDetails);


                Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
                if (validateToken) {

                    //set the authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                logger.info("Herre it is coming!");
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    logger.info("Validation fails !!");
                    throw new RuntimeException("Authorization header not present");
                }
            }
            System.out.println("Yaha tk to sb thik hi lgra h");
            filterChain.doFilter(request, response);
        } else {
            System.out.println("Yes not requiered header!");
            filterChain.doFilter(request, response);
        }
    }
}
