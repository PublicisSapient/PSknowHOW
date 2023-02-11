package com.publicissapient.kpidashboard.apis.pushdata.model;

import java.time.LocalDate;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * token details for push data via expose api
 */

@Data
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "expose_api_token")
public class ExposeApiToken extends BasicModel {

	private String userName;
	private String projectName;
	private ObjectId basicProjectConfigId;
	private String apiToken;
	private LocalDate expiryDate;
	private LocalDate createdAt;
	private LocalDate updatedAt;
}
