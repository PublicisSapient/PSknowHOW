package com.publicissapient.kpidashboard.common.model.application;

import java.util.Set;

import com.google.common.base.Objects;
import com.publicissapient.kpidashboard.common.constant.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AssigneeCapacity {
	private String userId;
	private String userName;
	private Set<String> email;
	private Role role;
	private String squad;
	private Double plannedCapacity;
	private Double leaves;
	private Double availableCapacity;
	private Integer happinessRating;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AssigneeCapacity) {
			AssigneeCapacity that = (AssigneeCapacity) obj;
			return Objects.equal(this.userId, that.userId) && Objects.equal(this.userName, that.userName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(userId, role);
	}
}
