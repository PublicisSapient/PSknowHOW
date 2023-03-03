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
public class AssigneeDetailsDTO {
    private String name;
    private String displayName;
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssigneeDetailsDTO) {
            AssigneeDetailsDTO that = (AssigneeDetailsDTO) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.displayName, that.displayName);
        }
        return false;
    }


}
