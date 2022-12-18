package com.example.demo2.reminders;

import com.example.demo2.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "reminders")
@EntityListeners(AuditingEntityListener.class)
public class Reminder extends BaseModel {
    @ManyToOne
    private User user;
    private String reminderMode;
    private Date reminderTime;
    private String status;


    //public abstract void sendReminderToUser();
    /*public void sendReminderToUser() {
        //find users whose reminders are enabled and reminder mode = email
        //filter out who didnt write journal as per schedule
        //send emails

        //first 2 steps, cann we maintain a table and insert next reminders along with status for a week
        //and then runs a job on this table
    }*/
}
