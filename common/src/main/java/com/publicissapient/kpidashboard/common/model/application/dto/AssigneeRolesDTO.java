package com.publicissapient.kpidashboard.common.model.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.Objects;

@Data
public class AssigneeRolesDTO {
    private String assignee;
    private List<String> roles;
}
