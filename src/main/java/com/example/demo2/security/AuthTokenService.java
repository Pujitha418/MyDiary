package com.example.demo2.security;

import com.example.demo2.security.exceptions.InvalidTokenException;
import com.example.demo2.user.User;
import com.example.demo2.user.UserRepository;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AuthTokenService {
    private AuthTokensRepository authTokensRepository;
    private UserRepository userRepository;

    @Autowired
    public AuthTokenService(AuthTokensRepository authTokensRepository, UserRepository userRepository) {
        this.authTokensRepository = authTokensRepository;
        this.userRepository = userRepository;
    }

    public String createToken(User user) throws UserNotFoundException {
        User userFromDb = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException(user.getId()));
        AuthTokens authToken = new AuthTokens();
        authToken.setUser(user);
        authToken.setStartTime(new Date());
        authToken.setTtl(24);
        AuthTokens savedToken = authTokensRepository.save(authToken);
        return savedToken.getToken().toString();
    }

    public String getToken(User user) {
        Date currTime = new Date();
        List<AuthTokens> tokens = authTokensRepository.findAllByUser(user);
        for (AuthTokens token:
             tokens) {
            if (validateToken(currTime, token.getStartTime(), token.getTtl())) {
                return token.getToken().toString();
            }
            authTokensRepository.delete(token);
        }
        return null;
    }

    public User getUserFromToken(String token) throws InvalidTokenException {
        AuthTokens tokenFromDb = authTokensRepository.findById(UUID.fromString(token)).orElseThrow(() -> new InvalidTokenException());
        Date currTime = new Date();
        if (validateToken(currTime, tokenFromDb.getStartTime(), tokenFromDb.getTtl())) {
            return tokenFromDb.getUser();
        }
        return null;
    }

    public void deleteToken(UUID token) {
        authTokensRepository.deleteById(token);
    }

    private Boolean validateToken(Date currTime, Date tokenTime, int ttl) {
        long difference_In_Time = currTime.getTime()-tokenTime.getTime();
        long difference_In_Hours
                = (difference_In_Time
                / (1000 * 60 * 60))
                % 24;
        if (difference_In_Hours < ttl) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


}
