package com.example.demo2.user.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class UserPreferencesRequestDto {
    private Map<String, String> preferences;
}
