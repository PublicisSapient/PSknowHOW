package com.publicissapient.kpidashboard.common.model;

import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ProcessorExecutionBasicConfig {
    private List<String> projectBasicConfigIds;
    private ExecutionLogContext logContext;
}
