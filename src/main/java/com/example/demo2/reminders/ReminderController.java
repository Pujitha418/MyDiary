package com.example.demo2.reminders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
@RequestMapping(path = "/reminders")
public class ReminderController {
    private ReminderService reminderService;
    private EmailService emailService;

    @Autowired
    public ReminderController(ReminderService reminderService, EmailService emailService) {
        this.reminderService = reminderService;
        this.emailService = emailService;
    }

    @PostMapping(path = "/sendEmails")
    @Scheduled(cron = "0 0 12 * * ?")
    public ResponseEntity<String> sendEmailReminders() {
        reminderService.sendEmailReminders();
        return ResponseEntity
                .accepted()
                .body("SUCCESS");
    }

    @PostMapping(path = "/sendSMS")
    @Scheduled(cron = "0 0 12 * * ?")
    public ResponseEntity<String> sendSMSReminders() {
        reminderService.sendSMSReminders();
        return ResponseEntity
                .accepted()
                .body("SUCCESS");
    }
}
