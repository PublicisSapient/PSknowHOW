package com.publicissapient.kpidashboard.common.model.rbac;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectBasicConfigNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3789596665503126275L;

	private String value;
	private List<ProjectBasicConfigNode> parent;
	private List<ProjectBasicConfigNode> children;
	private String groupName;

	/**
	 * Adds an entry to the Children Nodes list
	 * 
	 * @param child
	 */
	public void addChild(ProjectBasicConfigNode child) {
		if (!this.children.contains(child) && child != null) {
			this.children.add(child);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectBasicConfigNode other = (ProjectBasicConfigNode) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
