package com.example.demo2.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferencesResponseDto {
    private Map<String, String> preferences;
    private String error;
}
