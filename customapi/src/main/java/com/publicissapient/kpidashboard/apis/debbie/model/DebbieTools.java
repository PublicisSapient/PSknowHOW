package com.publicissapient.kpidashboard.apis.debbie.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "debbie_tools")
public class DebbieTools extends BasicModel {
    private String toolName;
    private String debbieProvider;
    private String testApiUrl;
}
