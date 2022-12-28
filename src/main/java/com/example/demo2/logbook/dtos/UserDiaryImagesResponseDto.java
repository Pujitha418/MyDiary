package com.example.demo2.logbook.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDiaryImagesResponseDto {
    byte[] diaryImage;
}
