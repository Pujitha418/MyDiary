package com.example.demo2.user;

import com.example.demo2.security.AuthTokenService;
import com.example.demo2.security.exceptions.InvalidTokenException;
import com.example.demo2.user.dtos.*;
import com.example.demo2.user.enums.UserPreferenceAttributes;
import com.example.demo2.user.exceptions.EmailAlreadyExistsException;
import com.example.demo2.user.exceptions.InvalidPasswordException;
import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PreferencesRepository preferencesRepository;
    private final AuthTokenService authTokenService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger;

    @Autowired
    public UserService(UserRepository userRepository, PreferencesRepository preferencesRepository, AuthTokenService authTokenService, ModelMapper modelMapper, PasswordEncoder passwordEncoder, Logger logger) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.authTokenService = authTokenService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.logger = logger;
    }

    public UserResponseDto createUser(CreateUserRequestDto userRequestDto) throws EmailAlreadyExistsException {
        User user = modelMapper.map(userRequestDto, User.class);
        Optional<User> userFromDb = userRepository.findByEmail(user.getEmail());
        if (userFromDb.isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        if (isAdminUser(user.getName())) {
            throw new RuntimeException("Invalid user name");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        List<UserPreferenceAttributes> preferenceAttributes = List.of(UserPreferenceAttributes.values());
        //List<Preferences> preferences = new ArrayList<Preferences>;
        for (UserPreferenceAttributes preferenceAttribute :
                preferenceAttributes) {
            savePreference(user, preferenceAttribute.toString(), "");
        }
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    public UserResponseDto getUser(UserRequestDto userRequestDto) throws UserNotFoundException {
        String email = userRequestDto.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException(email));
        return modelMapper.map(user, UserResponseDto.class);
    }
    public UserResponseDto login(LoginUserRequestDto loginUserRequestDto)
            throws UserNotFoundException,
            InvalidPasswordException {
        String email = loginUserRequestDto.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException(email));
        if (passwordEncoder.matches(loginUserRequestDto.getPassword(), user.getPassword())) {
            UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);
            userResponseDto.setToken(authTokenService.createToken(user));
            return userResponseDto;
        }
        else {
            throw new InvalidPasswordException();
        }
    }

    public void logout(UserRequestDto userRequestDto) {
        authTokenService.deleteToken(UUID.fromString(userRequestDto.getToken()));
    }

    public UserPreferencesResponseDto getUserPreferences(String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        if (isAdminUser(user.getName())) {
            throw new RuntimeException("Invalid user name");
        }
        return userPreferenceDtoConverter(getUserPreferences(user));
    }

    public UserPreferencesResponseDto updatePreference(UserPreferencesRequestDto userPreferencesRequestDto,
                                                       String authToken)
            throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }

        List<Preferences> userPreferencesFromDb = preferencesRepository.getPreferencesByUser(user);
        Map<String, String> newPreferences = userPreferencesRequestDto.getPreferences();
        System.out.println("newPreferences = " + newPreferences);
        System.out.println("userPreferencesFromDb = " + userPreferencesFromDb);
        for (Preferences preference:
                userPreferencesFromDb) {
            if (newPreferences.containsKey(preference.getAttribute())
                    && newPreferences.get(preference.getAttribute()) != null
                    && ! newPreferences.get(preference.getAttribute()).equals(preference.getValue())
            ) {
                savePreference(user, preference.getAttribute(), newPreferences.get(preference.getAttribute()));
            }
        }

        logger.info("Saved preferences");
        return userPreferenceDtoConverter(getUserPreferences(user));
    }

    private List<Preferences> getUserPreferences(User user) {
        return preferencesRepository.getPreferencesByUser(user);
    }

    private User getUserFromToken(String token) throws InvalidTokenException {
        token = StringUtils.removeStart(token, "Bearer").trim();
        return authTokenService.getUserFromToken(token);
    }

    private void savePreference(User user, String attribute, String value) {
        Preferences preference = new Preferences(user, attribute, value);
        preferencesRepository.save(preference);
    }

    private UserPreferencesResponseDto userPreferenceDtoConverter(List<Preferences> preferencesList) {
        UserPreferencesResponseDto userPreferencesResponse = new UserPreferencesResponseDto();
        Map<String, String> preferencesMap = new HashMap<>();
        for (Preferences preference:
                preferencesList) {
            preferencesMap.put(preference.getAttribute(), preference.getValue());
        }
        userPreferencesResponse.setPreferences(preferencesMap);
        return userPreferencesResponse;
    }

    public Boolean isAdminUser(String userName) {
        if (userName.toUpperCase().equals("ADMIN")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
