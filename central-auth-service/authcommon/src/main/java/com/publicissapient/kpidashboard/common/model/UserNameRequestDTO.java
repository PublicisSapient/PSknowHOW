package com.publicissapient.kpidashboard.common.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserNameRequestDTO {
    @NotNull
    private String userName;
}
