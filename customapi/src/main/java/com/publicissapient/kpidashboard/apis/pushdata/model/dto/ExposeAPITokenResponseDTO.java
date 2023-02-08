package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExposeAPITokenResponseDTO {

	private ObjectId basicProjectConfigId;
	private String projectName;
	private String username;
	private String apiToken;
	private String expiryDate;
	private String createdAt;
}
