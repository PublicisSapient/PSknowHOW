package com.publicissapient.kpidashboard.apis.repotools.model;

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
@Document(collection = "repo_tools_provider")
public class RepoToolsProvider extends BasicModel {
    private String toolName;
    private String repoToolProvider;
    private String testApiUrl;
}
