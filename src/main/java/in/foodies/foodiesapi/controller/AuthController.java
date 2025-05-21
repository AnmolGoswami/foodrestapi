package in.foodies.foodiesapi.controller;

import in.foodies.foodiesapi.dto.LoginRequest;
import in.foodies.foodiesapi.dto.RegisterRequest;
import in.foodies.foodiesapi.response.JwtResponse;
import in.foodies.foodiesapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginRequest(@RequestBody LoginRequest loginRequest){
        String jwt = userService.loginUser(loginRequest);

        return  new ResponseEntity<>(new JwtResponse(jwt,"Login Success"), HttpStatus.ACCEPTED);
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerRequest(@RequestBody RegisterRequest registerRequest) {
        try {
            String jwt = userService.registerUser(registerRequest);
            return new ResponseEntity<>(new JwtResponse(jwt, "You are registered successfully"), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Handle known errors like "User already exists"
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Handle unexpected errors
            return new ResponseEntity<>(Map.of("error", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
