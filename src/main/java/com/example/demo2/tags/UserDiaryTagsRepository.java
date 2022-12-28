package com.example.demo2.tags;

import com.example.demo2.logbook.Diary;
import com.example.demo2.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDiaryTagsRepository extends JpaRepository<UserDiaryTags, Long> {
    List<Tag> findAllByUserAndDiary(User user, Diary diary);

    List<Diary> findAllByUserAndTag(User user, Tag tag);
}
