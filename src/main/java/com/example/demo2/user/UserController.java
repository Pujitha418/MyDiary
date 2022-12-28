package com.example.demo2.user;

import com.example.demo2.gcs.GCSConfigProperties;
import com.example.demo2.gcs.controllers.GCSFileTransfer;
import com.example.demo2.user.dtos.*;
import com.example.demo2.user.exceptions.EmailAlreadyExistsException;
import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@RestController
//@RequestMapping("/user")
public class UserController {
    private GCSConfigProperties gcsConfigProperties;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Logger logger;
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final List<String> allowedHeaders = new ArrayList<>();
    private final GCSFileTransfer gcsFileTransfer;

    private final String userProfileImagesBucketName;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper, Logger logger, GCSConfigProperties gcsConfigProperties, GCSFileTransfer gcsFileTransfer) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.logger = logger;
        //Read userAvatarImages bucket name from GCSConfigProperties (this is autowired as it is marked as configuration.
        // Bean will be created automatically.
        this.gcsConfigProperties = gcsConfigProperties;
        //since this is mandatory, wrapped with assert.
        {
            assert gcsConfigProperties != null;
            this.userProfileImagesBucketName = gcsConfigProperties.userProfileImagesBucketName();
        }

        this.gcsFileTransfer = gcsFileTransfer;

        this.httpHeaders.add("isAdmin", String.valueOf(false));
        this.allowedHeaders.add("Origin");
        this.allowedHeaders.add("Content-Type");
        this.allowedHeaders.add("Authorization");
        this.allowedHeaders.add("Accept");
        List<String> exposedHeaders = new ArrayList<>();
        exposedHeaders.add("isAdmin");
        this.httpHeaders.setAccessControlExposeHeaders(exposedHeaders);
        //this.httpHeaders.setAccessControlAllowOrigin("http://localhost:3000");
        this.httpHeaders.setAccessControlAllowCredentials(true);
        List<HttpMethod> allowedMethods = new ArrayList<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        this.httpHeaders.setAccessControlAllowMethods(allowedMethods);
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
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        userService.logout(StringUtils.removeStart(authToken, "Bearer").trim());
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body("SUCCESS");
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

    @RequestMapping(path = "/user/profilePicture", method = RequestMethod.GET)
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<byte[]> getUserDp(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        try {
            if (authToken==null) {
                throw new Unauthorized();
            }
            String fileName = userService.getUserAvatarFileName(authToken);
            byte[] imgBytes = fileName!=null?gcsFileTransfer.downloadToMemory("my-diary-user-avatar", fileName):null;
            BufferedImage img = fileName!=null?ImageIO.read(new ByteArrayInputStream(imgBytes)):null;

            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(Base64Utils.encode(imgBytes));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(null);
        }
    }

    @RequestMapping(path = "/user/profilePicture", method = RequestMethod.POST)
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> updateUserDp(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                               @RequestParam("file") MultipartFile image,
                                               @RequestParam("name") String fileName) {
        System.out.println("inside updateUserDp = ");
        System.out.println("image = " + image);
        String uploadStatus = gcsFileTransfer.upload("my-diary-user-avatar", fileName, image);
        if (uploadStatus == "SUCCESS") {
            try {
                return ResponseEntity
                        .ok()
                        .headers(httpHeaders)
                        .body(userService.updateUserAvatarFileName(authToken, fileName));
            } catch (Exception e) {
                return ResponseEntity
                        .badRequest()
                        .headers(httpHeaders)
                        .body("Unable to update. Please try again later!");
            }
        }
        else
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(uploadStatus);
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
