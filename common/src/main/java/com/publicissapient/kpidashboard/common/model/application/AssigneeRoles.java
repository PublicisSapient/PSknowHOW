package com.publicissapient.kpidashboard.common.model.application;

import com.google.common.base.Objects;
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
public class AssigneeRoles {
    private String name;
    private String displayName;
    private String roles;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssigneeRoles) {
            AssigneeRoles that = (AssigneeRoles) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.displayName, that.displayName) && Objects.equal(this.roles, that.roles);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, roles);
    }

}
