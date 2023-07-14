
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

package com.publicissapient.kpidashboard.common.model.azureboards;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "displayName", "url", "id", "uniqueName", "imageUrl", "descriptor" })
public class SystemCreatedBy {

	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("url")
	private String url;
	@JsonProperty("id")
	private String id;
	@JsonProperty("uniqueName")
	private String uniqueName;
	@JsonProperty("imageUrl")
	private String imageUrl;
	@JsonProperty("descriptor")
	private String descriptor;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("displayName")
	public String getDisplayName() {
		return displayName;
	}

	@JsonProperty("displayName")
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	@JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("uniqueName")
	public String getUniqueName() {
		return uniqueName;
	}

	@JsonProperty("uniqueName")
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@JsonProperty("imageUrl")
	public String getImageUrl() {
		return imageUrl;
	}

	@JsonProperty("imageUrl")
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@JsonProperty("descriptor")
	public String getDescriptor() {
		return descriptor;
	}

	@JsonProperty("descriptor")
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("displayName", displayName).append("url", url).append("id", id)
				.append("uniqueName", uniqueName).append("imageUrl", imageUrl).append("descriptor", descriptor)
				.append("additionalProperties", additionalProperties).toString();
	}

}
