# My-Diary

My-Diary - Online Diary Application that lets users store memories every day.
This repository is for backend code.
 
### Backend:

Java Spring Boot Service. Supports 2 types of users:

##### 1. Registered users:

    Users can register and login.
    Supports Basic Authentication. Authorization is done using server side tokens.
    Basic user login functionalities are supported 
       (like user having/updating user profile pic, view/update user preference to enable/disable reminders, reminder mode/frequency)
   Users can add multiple memories per day, view recent memories and search by date range as well.
   Users can add images to a memory. As of now, restricted to one image per memory.

##### 2. Admin users:

    Admin users can launch batch job to send reminders for users who have enabled daily reminders.
    This is a scheduled cron job. Leverages kafka message queue capabilities to do emails job in background.

### Database:

Postgresql

### Other Features:
- User Memories are encrypted and stored inn database.
- User profile images and Images added to memories are served from Google cloud storage.

