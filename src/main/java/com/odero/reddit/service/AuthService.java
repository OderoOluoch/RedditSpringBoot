package com.odero.reddit.service;

import com.odero.reddit.dto.RegisterRequest;
import com.odero.reddit.model.User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
    public void signup(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setCreated(Instant.now());
        user.setEnabled(false);
    }
}
