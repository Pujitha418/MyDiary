package com.example.demo2.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {


    AuthenticationFilter(final RequestMatcher requiresAuth) {
        super(requiresAuth);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {

        //Optional<String> tokenParam = Optional.ofNullable(httpServletRequest.getHeader(AUTHORIZATION)); //Authorization: Bearer TOKEN
        String token= StringUtils.isNotEmpty(httpServletRequest.getHeader(AUTHORIZATION))? httpServletRequest.getHeader(AUTHORIZATION) : "";
        System.out.println("token before trimming = " + token);
        token= StringUtils.removeStart(token, "Bearer").trim();
        Authentication requestAuthentication = new UsernamePasswordAuthenticationToken(token, token, null);
        System.out.println("requestAuthentication.isAuthenticated() = " + requestAuthentication.isAuthenticated());
        ProviderManager manager = (ProviderManager) getAuthenticationManager();
        List<AuthenticationProvider> managerList = manager.getProviders();
        System.out.println("managerList = " + managerList.get(0).toString());
        Authentication response = getAuthenticationManager().authenticate(requestAuthentication);
        System.out.println("response.getDetails() + response.isAuthenticated() = " + response.getDetails() + response.isAuthenticated());
        return response;

    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }
}
