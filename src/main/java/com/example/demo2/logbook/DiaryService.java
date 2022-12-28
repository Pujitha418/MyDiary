package com.example.demo2.logbook;

import com.example.demo2.common.encryption.LongTextEncryptor;
import com.example.demo2.common.exceptions.Unauthorized;
import com.example.demo2.gcs.GCSConfigProperties;
import com.example.demo2.gcs.controllers.GCSFileTransfer;
import com.example.demo2.logbook.dtos.*;
import com.example.demo2.security.AuthTokenService;
import com.example.demo2.security.exceptions.InvalidTokenException;
import com.example.demo2.user.User;
import com.example.demo2.user.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final UserDiaryImagesRepository userDiaryImagesRepository;
    private final AuthTokenService authTokenService;
    private final ModelMapper modelMapper;
    private final Logger logger;
    //private final AttributeEncryptor attributeEncryptor;
    private final LongTextEncryptor textEncryptor;
    private final GCSFileTransfer gcsFileTransfer;
    private final String diaryImagesBucketName; //read from gcsConfigProperties

    @Autowired
    public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository, UserDiaryImagesRepository userDiaryImagesRepository, AuthTokenService authTokenService, ModelMapper modelMapper, Logger logger, LongTextEncryptor textEncryptor, GCSFileTransfer gcsFileTransfer, GCSConfigProperties gcsConfigProperties) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
        this.userDiaryImagesRepository = userDiaryImagesRepository;
        this.authTokenService = authTokenService;
        this.modelMapper = modelMapper;
        this.logger = logger;
        //this.attributeEncryptor = attributeEncryptor;
        this.textEncryptor = textEncryptor;
        this.gcsFileTransfer = gcsFileTransfer;
        this.diaryImagesBucketName = gcsConfigProperties.diaryImagesBucketName();
    }

    public DiaryResponseDto createDiary(CreateDiaryRequestDto createDiaryRequestDto, Optional<MultipartFile> diaryImage, String authToken) throws Unauthorized, InvalidTokenException {
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

        Diary createdDiary = createDiary(createDiaryRequestDto, user);
        DiaryResponseDto responseDto = modelMapper.map(createdDiary, DiaryResponseDto.class);
        responseDto.setNotes(textEncryptor.decrypt(responseDto.getNotes()));

        logger.info("diaryImage.isPresent()-"+diaryImage.isPresent());

        if (diaryImage.isPresent()) {
            createDiaryImage(user,
                             createdDiary,
                             createDiaryRequestDto.getFileName()!=null?
                                     createDiaryRequestDto.getFileName()+createdDiary.getUser().getId():
                                     createdDiary.getTitle()+createdDiary.getUser().getId(),
                             diaryImage.get());
        }
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

    public void createDiaryImage(User user, Diary diary, String fileName, MultipartFile file) {
        String uploadStatus = gcsFileTransfer.upload(diaryImagesBucketName, fileName, file);
        if (uploadStatus == "SUCCESS") {
            UserDiaryImages userDiaryImage = new UserDiaryImages();
            userDiaryImage.setUser(user);
            userDiaryImage.setDiary(diary);
            userDiaryImage.setImageFileName(fileName);

            userDiaryImagesRepository.save(userDiaryImage);
        }
    }

    public String deleteDiaryImage(String authToken, Long diaryId) throws InvalidTokenException, Unauthorized {
        logger.info("Inside deleteDiaryImage");
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        Optional<Diary> diary = diaryRepository.findById(diaryId);
        if (diary.isEmpty()) {
            return "No Diary with id="+diaryId+" exists";
        }
        Optional<List<UserDiaryImages>> userDiaryImages = userDiaryImagesRepository.findAllByUserAndDiary(user, diary.get());
        if (userDiaryImages.isPresent() && !userDiaryImages.get().isEmpty())
            deleteDiaryImage(userDiaryImages.get().get(0));
        return "SUCCESS";
    }

    private void deleteDiaryImage(UserDiaryImages userDiaryImage) {
        userDiaryImage.setMarkForDelete("Y");
        userDiaryImagesRepository.save(userDiaryImage);
        return;
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

    public SearchDiaryResponseDto getDiariesByUserLimitN(String authToken) throws Unauthorized, InvalidTokenException {
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        System.out.println("user.getId() = " + user.getId());

        Page<Diary> diaryList = diaryRepository.findAllByUserOrderByJournalDateDesc(user, PageRequest.of(1, 6));
        System.out.println("diaryList = " + diaryList.getContent());


        SearchDiaryResponseDto searchDiaryResponseDto = new SearchDiaryResponseDto();
        searchDiaryResponseDto.setDiaries(diaryToSearchDiaryResponseDto(diaryList.getContent()));
        return searchDiaryResponseDto;
    }

    public UserDiaryImagesResponseDto getUserDiaryImages(String authToken, Long diaryId) throws InvalidTokenException, Unauthorized {
        logger.info("Inside getUserDiaryImages");
        User user = getUserFromToken(authToken);
        if (user == null) {
            throw new Unauthorized();
        }
        Optional<Diary> diaryOptional = diaryRepository.findById(diaryId);
        if (diaryOptional.isEmpty()) {
            return null;
        }
        Optional<List<UserDiaryImages>> diaryImages = userDiaryImagesRepository.findAllByUserAndDiary(user, diaryOptional.get());
        logger.info("diaryImages-"+diaryImages);
        byte[] diaryImage = null;
        if (diaryImages.isPresent() && !diaryImages.get().isEmpty()) {
            diaryImage = gcsFileTransfer.downloadToMemory(diaryImagesBucketName, diaryImages.get().get(0).getImageFileName());
            diaryImage = Base64Utils.encode(diaryImage);
        }

        return new UserDiaryImagesResponseDto(diaryImage);
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