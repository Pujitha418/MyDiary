package com.example.demo2.logbook;

import com.example.demo2.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDiaryImagesRepository extends JpaRepository<UserDiaryImages, Long> {
    Optional<List<UserDiaryImages>> findAllByUserAndDiary(User user, Diary diary);
}
