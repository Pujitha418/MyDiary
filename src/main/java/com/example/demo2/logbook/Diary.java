package com.example.demo2.logbook;

import com.example.demo2.user.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Diary extends BaseModel{
    @ManyToOne
    @JoinColumn(updatable = false)
    private User user;
    private String title;
    @JoinColumn(updatable = false)
    @Temporal(TemporalType.DATE)
    private Date journalDate;

    @Type(type = "org.hibernate.type.TextType")
    @Convert(disableConversion = true)
    private String notes;
}
