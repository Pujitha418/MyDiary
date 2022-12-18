package com.example.demo2.logbook;

import com.example.demo2.common.encryption.AttributeEncryptor;
import com.example.demo2.common.encryption.LongTextEncryptor;
import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.logbook.dtos.*;
import com.example.demo2.security.AuthTokenService;
import com.example.demo2.security.exceptions.InvalidTokenException;
import com.example.demo2.user.User;
import com.example.demo2.user.UserRepository;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;
    private final ModelMapper modelMapper;
    private final Logger logger;
    //private final AttributeEncryptor attributeEncryptor;
    private final LongTextEncryptor textEncryptor;

    @Autowired
    public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository, AuthTokenService authTokenService, ModelMapper modelMapper, Logger logger, LongTextEncryptor textEncryptor) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.authTokenService = authTokenService;
        this.modelMapper = modelMapper;
        this.logger = logger;
        //this.attributeEncryptor = attributeEncryptor;
        this.textEncryptor = textEncryptor;
    }

    public DiaryResponseDto createDiary(CreateDiaryRequestDto createDiaryRequestDto, String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        //System.out.println("Encrypted notes = " + attributeEncryptor.convertToDatabaseColumn(createDiaryRequestDto.getNotes()));
        /*System.out.println("Decrypted notes = " +
                attributeEncryptor.convertToEntityAttribute("rOngGLc0Fu7dHd10AXpaW1zQ+DFMAEobrNl7lYVbvn4=")
        );*/
        String encNotes = textEncryptor.encrypt(createDiaryRequestDto.getNotes());
        String decNotes = textEncryptor.decrypt(encNotes);
        System.out.println("decNotes = " + decNotes);
        createDiaryRequestDto.setNotes(encNotes);
        DiaryResponseDto responseDto = modelMapper.map(createDiary(createDiaryRequestDto, user), DiaryResponseDto.class);
        responseDto.setNotes(textEncryptor.decrypt(responseDto.getNotes()));
        return responseDto;
    }

    public DiaryResponseDto updateDiary(UpdateDiaryRequestDto updateDiaryRequestDto, String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }

        Diary diary = modelMapper.map(updateDiaryRequestDto, Diary.class);
        Diary diaryFromDb;
        Optional<Diary> optionalDiary = diaryRepository.findById(diary.getId());
        if (optionalDiary.isEmpty()) {
            Diary createdDiary = createDiary(modelMapper.map(updateDiaryRequestDto, CreateDiaryRequestDto.class), user);
            return modelMapper.map(createdDiary, DiaryResponseDto.class);
        }
        else {
            diaryFromDb = optionalDiary.get();
        }
        String encNotes = textEncryptor.encrypt(diary.getNotes());
        diaryFromDb.setNotes(encNotes);
        diaryFromDb.setTitle(diary.getTitle());
        Diary savedDiary = diaryRepository.save(diaryFromDb);

        DiaryResponseDto responseDto = modelMapper.map(savedDiary, DiaryResponseDto.class);
        responseDto.setNotes(textEncryptor.decrypt(responseDto.getNotes()));
        return responseDto;
    }

    public SearchDiaryResponseDto searchDiaryByDate(SearchDiaryRequestDto searchDiaryRequestDto, String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        logger.info("Converting date");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-dd", Locale.ENGLISH);
        Date journalDateFrom;
        Date journalDateTo;

        try {
            //journalDateFrom = LocalDate.parse(searchDiaryRequestDto.getJournalDateFrom(), formatter);
            //journalDateTo = LocalDate.parse(searchDiaryRequestDto.getJournalDateTo(), formatter);
            journalDateFrom = format.parse(searchDiaryRequestDto.getJournalDateFrom());
            journalDateTo = format.parse(searchDiaryRequestDto.getJournalDateTo());
            logger.info("journalDateFrom - ", journalDateFrom);
            logger.info("journalDateTo - ", journalDateTo);
            List<Diary> diaryList = diaryRepository.findDiariesByUserAndJournalDateGreaterThanEqualAndJournalDateLessThan(
                    user,
                    journalDateFrom,
                    journalDateTo
                    //format.parse("2022-12-5"),
                    //format.parse("2022-12-6")
            );
            //System.out.println("diaryList = " + diaryList);

            SearchDiaryResponseDto searchDiaryResponseDto = new SearchDiaryResponseDto();
            searchDiaryResponseDto.setDiaries(diaryToSearchDiaryResponseDto(diaryList));
            return searchDiaryResponseDto;
        } catch (DateTimeException e) {
            logger.warn("date exception - ");
            throw new RuntimeException("Invalid Date Format - " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public SearchDiaryResponseDto getDiariesByUser(String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        System.out.println("user.getId() = " + user.getId());

        List<Diary> diaryList = diaryRepository.findDiariesByUserOrderByJournalDateDesc(user);
        System.out.println("diaryList = " + diaryList);


        SearchDiaryResponseDto searchDiaryResponseDto = new SearchDiaryResponseDto();
        searchDiaryResponseDto.setDiaries(diaryToSearchDiaryResponseDto(diaryList));
        return searchDiaryResponseDto;
    }

    private Diary createDiary(CreateDiaryRequestDto createDiaryRequestDto, User user) {
        Diary diary = modelMapper.map(createDiaryRequestDto, Diary.class);
        diary.setUser(user);
        Diary savedDiary = diaryRepository.save(diary);
        return savedDiary;
    }

    private List<DiaryResponseDto> diaryToSearchDiaryResponseDto(List<Diary> diaryList) {
        List<DiaryResponseDto> diaryResponseList = new ArrayList<>();

        for (Diary diary:
                diaryList) {
            DiaryResponseDto diaryResponseDto = modelMapper.map(diary, DiaryResponseDto.class);
            String origNotes = diaryResponseDto.getNotes();
            diaryResponseDto.setNotes(textEncryptor.decrypt(origNotes));
            diaryResponseList.add(diaryResponseDto);
        }

        return diaryResponseList;
    }

    private User getUserFromToken(String token) throws InvalidTokenException {
        token = StringUtils.removeStart(token, "Bearer").trim();
        return authTokenService.getUserFromToken(token);
    }
}