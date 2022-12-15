package com.publicissapient.kpidashboard.common.model;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ProcessorExecutionBasicConfig {
	private List<String> projectBasicConfigIds;
	private ExecutionLogContext logContext;
}
