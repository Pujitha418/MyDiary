package com.example.demo2.user;

import com.example.demo2.user.dtos.*;
import com.example.demo2.user.exceptions.EmailAlreadyExistsException;
import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RestController
//@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Logger logger;
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final List<String> allowedHeaders = new ArrayList<>();

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, Logger logger) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.logger = logger;
        this.httpHeaders.add("isAdmin", String.valueOf(false));
        this.allowedHeaders.add("Origin");
        this.allowedHeaders.add("Content-Type");
        this.allowedHeaders.add("Authorization");
        this.allowedHeaders.add("Accept");
        List<String> exposedHeaders = new ArrayList<>();
        exposedHeaders.add("isAdmin");
        this.httpHeaders.setAccessControlExposeHeaders(exposedHeaders);
        //httpHeaders.setAccessControlAllowOrigin("http://localhost:3000");
        this.httpHeaders.setAccessControlAllowCredentials(true);
        this.httpHeaders.setAccessControlAllowMethods(Collections.singletonList(HttpMethod.GET));
        this.httpHeaders.setAccessControlAllowHeaders(allowedHeaders);

    }

    @RequestMapping(value = "/user/create", method = RequestMethod.POST)
    @CrossOrigin(origins = "http://localhost:3000")
    //@PostMapping(name = "/create")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserRequestDto userRequestDto) {
        try {
            UserResponseDto createdUser = userService.createUser(userRequestDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdUser);
        }
        catch (EmailAlreadyExistsException e) {
            logger.warn("Raising EmailAlreadyExistsException");
            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setError(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(userResponseDto);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    //@GetMapping(name = "/")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestBody UserRequestDto userRequestDto) {
        logger.info("inside getUserByEmail");
        try {
            UserResponseDto userResponseDto = userService.getUser(modelMapper.map(userRequestDto, UserRequestDto.class));
            return ResponseEntity
                    .ok()
                    .body(userResponseDto);
        }
        catch (UserNotFoundException e) {
            logger.warn("Raising UserNotFoundException");
            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setError(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(userResponseDto);
        }
    }

    //@PostMapping(name = "/login")
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<UserResponseDto> loginUser(@RequestBody LoginUserRequestDto loginUserRequestDto) {
        try {
            UserResponseDto userResponseDto = userService.login(loginUserRequestDto);
            if (userService.isAdminUser(userResponseDto.getName())) {
                httpHeaders.set("isAdmin", String.valueOf(true));
            }
            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(userResponseDto);
        } catch (Exception e) {
            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setError(e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(userResponseDto);
        }
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<String> logout(@RequestBody UserRequestDto userRequestDto) {
        userService.logout(userRequestDto);
        return ResponseEntity.ok().body("SUCCESS");
    }

    //@GetMapping(value = "/preferences/{id}")
    @RequestMapping(value = "/user/preferences", method = RequestMethod.GET)
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<UserPreferencesResponseDto> getUserPreferences(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        try {
            if (authToken==null) {
                throw new Unauthorized();
            }
            return ResponseEntity
                    .ok()
                    .body(userService.getUserPreferences(authToken));
        } catch (Exception e) {
            UserPreferencesResponseDto preferencesResponse = new UserPreferencesResponseDto();
            preferencesResponse.setError(e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(preferencesResponse);
        }
    }

    //@PostMapping(value = "/updatePreferences/")
    @RequestMapping(value = "/user/updatePreferences/", method = RequestMethod.POST)
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<UserPreferencesResponseDto> updateUserPreferences(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                            @RequestBody UserPreferencesRequestDto userPreferencesRequestDto) {
        try {
            if (authToken==null) {
                throw new Unauthorized();
            }
            return ResponseEntity
                    .ok()
                    .body(userService.updatePreference(userPreferencesRequestDto, authToken));
        } catch (Exception e) {
            UserPreferencesResponseDto preferencesResponse = new UserPreferencesResponseDto();
            preferencesResponse.setError(e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(preferencesResponse);
        }
    }

    @ExceptionHandler ({
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> buildError(Exception e) {
        if (e instanceof UserNotFoundException) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto(e.getMessage()));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(e.getMessage()));
    }
}
