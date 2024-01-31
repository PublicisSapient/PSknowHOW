package com.publicissapient.kpidashboard.notification.service;

import com.publicissapient.kpidashboard.notification.model.EmailEvent;

public interface NotificationService {

    void sendMail(String key, EmailEvent emailEvent);

    void sendMailUsingSendGrid(String key, EmailEvent emailEvent);

}
