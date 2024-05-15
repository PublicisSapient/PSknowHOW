/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.service.impl;

import com.publicissapient.kpidashboard.apis.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * this service uses for one single Properties Configuration file message to
 * every class Use @Lazy Annotation in Your Service Class and Inject MyService
 * into Other Classes ex class UserServiceImpl
 */
@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {
	protected final MessageSource mSource;

	@Override
	public String getMessage(String id) {
		Locale locale = LocaleContextHolder.getLocale();
		return mSource.getMessage(id, null, locale);
	}

	@Override
	public String getMessage(String id, Object[] arg) {
		Locale locale = LocaleContextHolder.getLocale();
		return mSource.getMessage(id, arg, locale);
	}
}
