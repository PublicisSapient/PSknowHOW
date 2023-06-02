package com.publicissapient.kpidashboard.jira.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FirstJobListener implements JobExecutionListener {

	public void beforeJob(JobExecution je) {
		System.out.println( "Before Job ");
		System.out.println( "Job Name : "+je.getJobInstance().getJobName() );
		System.out.println( "Job parameters : "+je.getJobParameters());
		System.out.println( "Job context : "+je.getExecutionContext());
		
		je.getExecutionContext().put("je key", "job value");
		
	}

	public void afterJob(JobExecution je) {
		System.out.println( "After Job ");
		System.out.println( "Job Name : "+je.getJobInstance().getJobName() );
		System.out.println( "Job parameters : "+je.getJobParameters());
		System.out.println( "Job context : "+je.getExecutionContext());
	}

}
