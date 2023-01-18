package com.publicissapient.kpidashboard.common.model.application;

import com.google.common.base.Objects;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AssigneeDetails {
    private String name;
    private String displayName;
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssigneeDetails) {
            AssigneeDetails that = (AssigneeDetails) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.displayName, that.displayName);
        }
        return false;
    }


}
