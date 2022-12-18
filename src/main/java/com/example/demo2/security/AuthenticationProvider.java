package com.example.demo2.security;

import com.example.demo2.security.exceptions.InvalidTokenException;
import com.example.demo2.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    @Autowired
    private AuthTokenService authTokenService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails user = retrieveUser(null, (UsernamePasswordAuthenticationToken) authentication);
        System.out.println("user from authenticate = " + user);
        if (user != null) {
            return createSuccessAuthentication(user.getUsername(), authentication, user);
        }
        return null;
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal, authentication.getCredentials(), null);
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user");
        System.out.println("Authenticated user");
        return result;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object token = authentication.getCredentials();
        System.out.println("token = " + token);
        if (token == null) {
            return null;
        }
        try {
            User user = authTokenService.getUserFromToken(token.toString());
            UserDetails userDetails = new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return null;
                }

                @Override
                public String getPassword() {
                    return user.getPassword();
                }

                @Override
                public String getUsername() {
                    return user.getName();
                }

                @Override
                public boolean isAccountNonExpired() {
                    return false;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return false;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return false;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };
            System.out.println("userDetails.getUsername() = " + userDetails.getUsername());
            return userDetails;
        }
        catch (InvalidTokenException e) {
            //throw new RuntimeException(e);
            System.out.println("Invalid Token");
            return null;
        }
    }
}
