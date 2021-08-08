package com.odero.reddit.service;

import com.odero.reddit.dto.AuthenticationResponse;
import com.odero.reddit.dto.LoginRequest;
import com.odero.reddit.dto.RegisterRequest;
import com.odero.reddit.exception.SpringRedditException;
import com.odero.reddit.model.NotificationEmail;
import com.odero.reddit.model.User;
import com.odero.reddit.model.VerificationToken;
import com.odero.reddit.repository.UserRepository;
import com.odero.reddit.repository.VerificationTokenRepository;
import com.odero.reddit.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Reddit clone email Verification", user.getEmail(),
                "Oh, you are here." +
                        "Click on this link to activate your account  " +
                        "http://localhost:8080/api/auth/accountVerification/" +token
                ));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
       Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
       verificationToken.orElseThrow(()-> new SpringRedditException("Invalid Token"));
       fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    void fetchUserAndEnable(VerificationToken verificationToken) {
       String username = verificationToken.getUser().getUsername();
       User user = userRepository.findByUsername(username).orElseThrow(()-> new SpringRedditException("No user found with the name " +username));
       user.setEnabled(true);
       userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        return new AuthenticationResponse(token,loginRequest.getUsername());
    }
}
