package org.example.usermanagement.service;

import org.example.usermanagement.dto.request.RegisterRequest;
import org.example.usermanagement.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}