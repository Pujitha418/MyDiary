package com.example.demo2.logbook.dtos;

import lombok.Data;

@Data
public class SearchDiaryRequestDto {
    private String journalDateFrom;
    private String journalDateTo;
}
