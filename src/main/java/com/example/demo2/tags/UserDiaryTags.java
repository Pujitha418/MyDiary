package com.example.demo2.tags;

import com.example.demo2.logbook.Diary;
import com.example.demo2.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity(name = "user_diary_tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDiaryTags extends BaseModel {
    @ManyToOne
    private User user;
    @ManyToOne
    private Diary diary;
    @ManyToOne
    private Tag tag;
}
