package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.dto.LoginRequest;
import in.foodies.foodiesapi.dto.RegisterRequest;
import in.foodies.foodiesapi.entity.User;
import in.foodies.foodiesapi.repositary.UserRepositary;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service

public class UserServiceImpl implements  UserService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepositary userRepositary;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String loginUser(LoginRequest loginRequest) {

        Authentication  authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),loginRequest.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails =(UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);



        return jwt;
    }

    @Override
    public String registerUser(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();

        // Check if user already exists
        try {
            userDetailsService.loadUserByUsername(email);
            // If no exception, user exists
            throw new RuntimeException("User already exists with this email.");
        } catch (UsernameNotFoundException e) {
            // User does not exist, proceed with registration
            User user = new User();
            user.setFullName(registerRequest.getFullName());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            User savedUser = userRepositary.save(user);

            // Generate and return JWT token
            return jwtService.generateToken(savedUser);
        }
    }

}
