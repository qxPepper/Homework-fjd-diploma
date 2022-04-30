package ru.netology.homeworkfjddiploma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.homeworkfjddiploma.security.AuthRequest;
import ru.netology.homeworkfjddiploma.security.JWTUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @PostMapping("login")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Имя или пароль неправильны", e);
        }
        String token = jwtTokenUtil.generateToken((UserDetails) authentication.getPrincipal());

        Map<String, String> mapToken = new HashMap<>();
        mapToken.put("auth-token", token);
        return mapToken;
    }
}
