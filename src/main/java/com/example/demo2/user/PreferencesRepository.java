package com.example.demo2.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    List<Preferences> getPreferencesByUser(User user);

    @Query(value = "select distinct user from user_preferences where attribute='REMINDER_MODE' and value='Email'" +
            "and user in  (select user from user_preferences where attribute='ENABLE_REMINDERS' and value='Y')" ,
            nativeQuery = true
    )
    List<User> getUsersWithEmailReminderPreference();

    @Query(value = "select distinct user from user_preferences where attribute='REMINDER_MODE' and value='Email'" +
            " and user_id in (select user from user_preferences where attribute='ENABLE_REMINDERS' and value='Y')" +
            " and user_id not in (select user from reminders where status in ('SCHEDULED','SENT') and date(reminderTime) = current_date )"
            )
    List<User> getUsersWithEmailReminderPreferenceUnfulfilled();

    @Query(value = "select distinct user from user_preferences where attribute='REMINDER_MODE' and value='SMS'" +
            " and user_id in (select user from user_preferences where attribute='ENABLE_REMINDERS' and value='Y')" +
            " and user_id not in (select user from reminders where status in ('SCHEDULED','SENT') and date(reminderTime) = current_date )"
    )
    List<User> getUsersWithSMSReminderPreferenceUnfulfilled();

    /*@Query(
            value = "SELECT distinct user_id from mydiary.user_preferences where attribute='REMINDER_MODE' and value='Email' " +
                    "and id in (select user_id from mydiary.user_preferences where attribute='ENABLE_REMINDERS' and value='Y')",
            nativeQuery = true
    )*/
}