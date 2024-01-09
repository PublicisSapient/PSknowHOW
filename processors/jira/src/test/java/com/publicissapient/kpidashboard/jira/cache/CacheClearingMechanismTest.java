package com.publicissapient.kpidashboard.jira.cache;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CacheClearingMechanismTest {

    @InjectMocks
    private CacheClearingMechanism cacheClearingMechanism;

    @Mock
    private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;


    @Test
    public void testSignalJobCompletion_ClearCacheWhenJobCountIsZero() {
        int jobCount = 0;
        cacheClearingMechanism.setJobCount(jobCount);
        cacheClearingMechanism.signalJobCompletion();

        verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.JIRA_KPI_CACHE);
    }

    @Test
    public void testSignalJobCompletion_DoesNotClearCacheWhenJobCountIsNotZero() {
        int jobCount = 2;
        cacheClearingMechanism.setJobCount(jobCount);
        cacheClearingMechanism.signalJobCompletion();

        verify(jiraProcessorCacheEvictor, times(0)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        verify(jiraProcessorCacheEvictor, times(0)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.JIRA_KPI_CACHE);
    }

    @Test
    public void testSignalJobCompletion_ClearCacheWhenJobCountBecomesZero() {
        int jobCount = 3;
        cacheClearingMechanism.setJobCount(jobCount);
        cacheClearingMechanism.signalJobCompletion(); // Job 1 completed
        cacheClearingMechanism.signalJobCompletion(); // Job 2 completed
        cacheClearingMechanism.signalJobCompletion(); // Job 3 completed, now cache should be cleared

        verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
                CommonConstant.JIRA_KPI_CACHE);
    }
}
