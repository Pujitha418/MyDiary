package com.example.demo2.logbook;

import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.logbook.dtos.*;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/diary")
public class DiaryController {
    private final DiaryService diaryService;
    private final Logger logger;

    private HttpHeaders httpHeaders = new HttpHeaders();
    private List<String> allowedHeaders = new ArrayList<>();

    @Autowired
    public DiaryController(DiaryService diaryService, Logger logger) {
        this.diaryService = diaryService;
        this.logger = logger;
        this.allowedHeaders.add("Origin");
        this.allowedHeaders.add("Content-Type");
        this.allowedHeaders.add("Authorization");
        this.allowedHeaders.add("Accept");
        //this.httpHeaders.setAccessControlAllowOrigin("http://localhost:3000");
        this.httpHeaders.setAccessControlAllowCredentials(true);
        this.httpHeaders.setAccessControlAllowMethods(Collections.singletonList(HttpMethod.GET));
        this.httpHeaders.setAccessControlAllowHeaders(allowedHeaders);
    }

    @PostMapping(path = "/create")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<DiaryResponseDto> createDiary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                        @RequestPart("diary") CreateDiaryRequestDto createDiaryRequestDto,
                                                        @RequestPart("file") Optional<MultipartFile> file) {
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }

            DiaryResponseDto response = diaryService.createDiary(createDiaryRequestDto, file, authToken);
            return ResponseEntity
                    .created(new URI("/diary/"+response.getId()))
                    .headers(httpHeaders)
                    .body(response);
        }
        catch (Exception e) {
            logger.error(e.getMessage());

            DiaryResponseDto response = new DiaryResponseDto();
            if (e instanceof UserNotFoundException) {
                response.setError(e.getMessage());
            }
            else {
                response.setError("Unable to create diary");
            }
            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }

    @PostMapping(path = "/update")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<DiaryResponseDto> updateDiary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                        @RequestBody UpdateDiaryRequestDto updateDiaryRequestDto) {
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }
            DiaryResponseDto response = diaryService.updateDiary(updateDiaryRequestDto, authToken);
            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(response);
        }
        catch (Exception e) {
            logger.error(e.getMessage());

            DiaryResponseDto response = new DiaryResponseDto();
            if (e instanceof UserNotFoundException) {
                response.setError(e.getMessage());
            }
            else {
                response.setError("Unable to update diary");
            }
            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }

    @GetMapping(path = "/searchByDateRange")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<SearchDiaryResponseDto> getDiariesByJournalDates(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                           @RequestBody SearchDiaryRequestDto searchDiaryRequestDto) {
        if (searchDiaryRequestDto.getJournalDateFrom() == null && searchDiaryRequestDto.getJournalDateTo() == null) {
            return null;
        } else if (searchDiaryRequestDto.getJournalDateTo() == null) {
            searchDiaryRequestDto.setJournalDateTo(searchDiaryRequestDto.getJournalDateFrom());
        }
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }

            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(diaryService.searchDiaryByDate(searchDiaryRequestDto, authToken));
        } catch (Exception e) {
            logger.error(e.getMessage());

            SearchDiaryResponseDto response = new SearchDiaryResponseDto();
            response.setError(e.getMessage());
            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }

    @GetMapping(path = "/searchByUser")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<SearchDiaryResponseDto> getDiariesByUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }

            //SearchDiaryResponseDto diaries = diaryService.getDiariesByUserLimitN(authToken);
            SearchDiaryResponseDto diaries = diaryService.getDiariesByUser(authToken);
            //System.out.println("diaries.getDiaries().get(0) = " + diaries.getDiaries().get(0));

            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(diaries);
        } catch (Exception e) {
            logger.error(e.getMessage());

            SearchDiaryResponseDto response = new SearchDiaryResponseDto();
            response.setError(e.getMessage());
            return ResponseEntity
                    .internalServerError()
                    .body(response);
        }
    }

    //GET with request body not supported either via fetch or axios. Hence changing to request param.
    @GetMapping(path = "/getDiaryImages")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<byte[]> getDiaryImages(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                                     @RequestParam("id") Long id) {
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }

            return ResponseEntity
                    .ok()
                    .headers(httpHeaders)
                    .body(diaryService.getUserDiaryImages(authToken, id).getDiaryImage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .headers(httpHeaders)
                    .body(null);
        }
    }

    @PostMapping(path = "/deleteDiaryImage")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> deleteDiaryImages(@RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
                                                    Long diaryId) {
        try {
            if (authToken == null) {
                throw new Unauthorized();
            }

            String response = diaryService.deleteDiaryImage(authToken, diaryId);

            if (response.equals("SUCCESS"))
                return ResponseEntity
                        .ok()
                        .headers(httpHeaders)
                        .body(response);
            else
                return ResponseEntity
                        .unprocessableEntity()
                        .headers(httpHeaders)
                        .body(response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body("FAILED");
        }
    }
}
