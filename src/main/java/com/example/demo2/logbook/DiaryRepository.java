package com.example.demo2.logbook;

import com.example.demo2.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findDiariesByUserOrderByJournalDateDesc(User user);

    Page<Diary> findAllByUserOrderByJournalDateDesc(User user, Pageable pageable);

    List<Diary> findDiariesByUserAndJournalDateGreaterThanEqualAndJournalDateLessThan(User user, Date journalDateFrom, Date journalDateTo);
}
