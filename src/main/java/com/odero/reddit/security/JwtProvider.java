package com.odero.reddit.security;

//import com.odero.reddit.model.User;
import com.odero.reddit.exception.SpringRedditException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.IIOException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {
    private KeyStore keyStore;

    @PostConstruct
    public void init() throws IOException {
        try{
            keyStore = KeyStore.getInstance("jks");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream,"secret".toCharArray());
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IIOException e){
                throw new SpringRedditException("Exception occurred while loading keyStore || " + e.getMessage());
        }
    }

    public String generateToken(Authentication authentication){
        User principal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();
    }
    private PrivateKey getPrivateKey(){
        try {
            return (PrivateKey) keyStore.getKey("springblog","secret".toCharArray());
        }catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e){
            throw new SpringRedditException("Exception occurred while loading keyStore || " + e.getMessage());
        }
    }
}