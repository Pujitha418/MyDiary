package com.example.demo2.logbook.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SearchDiaryResponseDto {
    private List<DiaryResponseDto> diaries;
    private String error;
}
