package com.example.demo2.kafka.email.services;

import com.example.demo2.reminders.ReminderService;
import com.example.demo2.reminders.enums.ReminderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailPostCallableExecutionFactory {
    private final ReminderService reminderService;

    @Autowired
    public EmailPostCallableExecutionFactory(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public void execute(String methodName, Long id, String emailStatus) {
        switch (methodName) {
            case "updateReminderStatus":
                reminderService.updateReminderStatus(
                        id,
                        emailStatus=="SENT"? ReminderStatus.SENT:ReminderStatus.FAILED
                );
        }
    }
}
