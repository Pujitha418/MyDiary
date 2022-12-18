package com.example.demo2.logbook.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiaryRequestDto {
    private Long id;
    private String title;
    private Date journalDate;
    private String notes;
}
