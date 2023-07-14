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
package com.publicissapient.kpidashboard.notification.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * This class is represent kafka message format
 *
 * @author pkum34
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailEvent  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1334987659830782650L;
	
	private String from;
	private List<String> to;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
	private String body;
	private Map<String,String> customData;
	private String emailHost;
	private int emailPort;

}
