package com.example.demo2.logbook;

import com.example.demo2.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

@Entity(name = "user_diary_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDiaryImages extends BaseModel {
    @ManyToOne
    private User user;
    @ManyToOne
    private Diary diary;
    private String imageFileName;
    private String markForDelete;

}
