package com.hotel.auth.service;

import com.hotel.auth.dto.LoginRequest;
import com.hotel.auth.dto.RegisterRequest;
import com.hotel.auth.model.User;

public interface AuthService {

    User register(RegisterRequest request);

    User login(LoginRequest request);
}
