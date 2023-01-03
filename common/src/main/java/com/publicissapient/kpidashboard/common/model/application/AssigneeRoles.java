package com.publicissapient.kpidashboard.common.model.application;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AssigneeRoles {
    private String assignee;
    private List<String> roles;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssigneeRoles) {
            AssigneeRoles that = (AssigneeRoles) obj;
            return Objects.equal(this.assignee, that.assignee) && Objects.equal(this.roles, that.roles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(assignee, roles);
    }

}
