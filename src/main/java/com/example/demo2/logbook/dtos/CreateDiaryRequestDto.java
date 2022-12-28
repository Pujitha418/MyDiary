package com.example.demo2.logbook.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiaryRequestDto {
    private String title;
    private Date journalDate;
    private String notes;
    private String fileName;
}
