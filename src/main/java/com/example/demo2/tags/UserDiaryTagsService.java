package com.example.demo2.tags;

import com.example.demo2.logbook.Diary;
import com.example.demo2.logbook.DiaryRepository;
import com.example.demo2.user.User;
import com.example.demo2.user.UserRepository;
import com.example.demo2.user.exceptions.UserNotFoundException;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDiaryTagsService {
    private UserRepository userRepository;
    private DiaryRepository diaryRepository;
    private TagRepository tagRepository;
    private UserDiaryTagsRepository userDiaryTagsRepository;
    private Logger logger;

    @Autowired
    public UserDiaryTagsService(UserRepository userRepository, DiaryRepository diaryRepository, TagRepository tagRepository, UserDiaryTagsRepository userDiaryTagsRepository, Logger logger) {
        this.userRepository = userRepository;
        this.diaryRepository = diaryRepository;
        this.tagRepository = tagRepository;
        this.userDiaryTagsRepository = userDiaryTagsRepository;
        this.logger = logger;
    }

    public List<Tag> getDiaryTags(Long userId, Long diaryId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new UserNotFoundException(userId));
        Diary diary = diaryRepository.findById(diaryId)
                                     .orElseThrow(() -> new RuntimeException("Diary Not Found"));
        return userDiaryTagsRepository.findAllByUserAndDiary(user, diary);
    }

    public List<Diary> getDiariesByTag (Long userId, Long tagId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new UserNotFoundException(userId));
        Tag tag = tagRepository.findById(tagId)
                                .orElseThrow(() -> new RuntimeException("Tag Not Found"));
        return userDiaryTagsRepository.findAllByUserAndTag(user, tag);
    }
}
