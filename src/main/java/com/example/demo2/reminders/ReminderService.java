package com.example.demo2.reminders;

import com.example.demo2.kafka.email.controllers.SendEmailService;
import com.example.demo2.kafka.email.models.SendEmailApiParams;
import com.example.demo2.reminders.enums.ReminderMode;
import com.example.demo2.reminders.enums.ReminderStatus;
import com.example.demo2.user.PreferencesRepository;
import com.example.demo2.user.User;
import com.example.demo2.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final PreferencesRepository preferencesRepository;
    private final SendEmailService emailService;
    private ModelMapper modelMapper;

    @Autowired
    public ReminderService(ReminderRepository reminderRepository, UserRepository userRepository, PreferencesRepository preferencesRepository, SendEmailService emailService, ModelMapper modelMapper) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
    }

    public void sendEmailReminders() {
        //find users whose reminders are enabled and reminder mode = email
        //filter out who didnt write journal as per schedule
        //send emails

        //first 2 steps, cann we maintain a table and insert next reminders along with status for a week
        //and then runs a job on this table

        createSchedulesAndPublishEvent(ReminderMode.Email, ReminderStatus.SCHEDULED);
        //send to kafka and write email consumer
    }

    public void sendSMSReminders() {
        //find users whose reminders are enabled and reminder mode = SMS
        //send SMS

        createSchedulesAndPublishEvent(ReminderMode.Email, ReminderStatus.SCHEDULED);
        //send to kafka and write SMS consumer
    }

    private void createSchedulesAndPublishEvent(ReminderMode reminderMode, ReminderStatus reminderStatus) {
        //System.out.println(preferencesRepository.getUsersWithEmailReminderPreferenceUnfulfilled());
        List<User> users = preferencesRepository.getUsersWithEmailReminderPreferenceUnfulfilled();
        //List<User> users = new ArrayList<>();
        for (User user:
                users) {
            Reminder reminder = new Reminder(user, ReminderMode.Email.toString(), new Date(), ReminderStatus.SCHEDULED.toString());
            reminderRepository.save(reminder);
            emailService.sendEmailEventToKafka(reminderToSendEmailApiParamsConverter(reminder));
        }
    }

    private SendEmailApiParams reminderToSendEmailApiParamsConverter(Reminder reminder) {
        SendEmailApiParams params = new SendEmailApiParams();
        params.setToEmail(reminder.getUser().getEmail());
        params.setSubject("Your Journey Reminder - "+reminder.getReminderTime().toString());
        params.setName(reminder.getUser().getName());
        return params;
    }
}
