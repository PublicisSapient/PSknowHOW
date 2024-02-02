package com.publicissapient.kpidashboard.notification.service;

import com.publicissapient.kpidashboard.notification.model.EmailEvent;

public interface NotificationService {
	
	public void sendMail(String key,EmailEvent emailEvent);

}
