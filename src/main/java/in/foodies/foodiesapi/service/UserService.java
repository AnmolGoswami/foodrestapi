package in.foodies.foodiesapi.service;

import in.foodies.foodiesapi.dto.LoginRequest;
import in.foodies.foodiesapi.dto.RegisterRequest;

public interface UserService {

    public String loginUser(LoginRequest loginRequest);

    public String registerUser(RegisterRequest registerRequest);
}
