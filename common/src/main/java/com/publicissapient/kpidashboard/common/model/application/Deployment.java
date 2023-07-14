package com.publicissapient.kpidashboard.common.model.application;

import java.util.Objects;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "deployments")
public class Deployment extends BasicModel {

	private ObjectId processorId;
	private ObjectId basicProjectConfigId;
	private ObjectId projectToolConfigId;
	private String envId;
	private String envName;
	private String envUrl;
	private String startTime;
	private String endTime;
	private long duration;
	private DeploymentStatus deploymentStatus;
	private String jobId;
	private String jobName;
	private String jobFolderName;
	private String deployedBy;
	private String number;
	private String createdAt;
	private String updatedTime;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Deployment that = (Deployment) o;
		return projectToolConfigId.equals(that.projectToolConfigId) && number.equals(that.number);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectToolConfigId, number);
	}

	@Override
	public String toString() {
		return "Deployment{" + "processorId=" + processorId + ", basicProjectConfigId=" + basicProjectConfigId
				+ ", projectToolConfigId=" + projectToolConfigId + ", envId='" + envId + '\'' + ", envName='" + envName
				+ '\'' + ", envUrl='" + envUrl + '\'' + ", startTime='" + startTime + '\'' + ", endTime='" + endTime
				+ '\'' + ", duration=" + duration + ", deploymentStatus=" + deploymentStatus + ", jobId='" + jobId
				+ '\'' + ", jobName='" + jobName + '\'' + ", jobFolderName='" + jobFolderName + '\'' + ", deployedBy='"
				+ deployedBy + '\'' + ", number='" + number + '\'' + ", createdAt='" + createdAt + '\'' + '}';
	}
}
