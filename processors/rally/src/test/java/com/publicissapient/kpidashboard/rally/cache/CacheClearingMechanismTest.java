package com.publicissapient.kpidashboard.rally.cache;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;

@ExtendWith(MockitoExtension.class)
public class CacheClearingMechanismTest {

    @InjectMocks
    private CacheClearingMechanism cacheClearingMechanism;

    @Mock
    private RallyProcessorCacheEvictor rallyProcessorCacheEvictor;

    @BeforeEach
    public void setup() {
        cacheClearingMechanism.setJobCount(2); // Set initial job count to 2
    }

    @Test
    public void testSignalJobCompletionWhenNotAllJobsComplete() {
        // Execute one job completion
        cacheClearingMechanism.signalJobCompletion();

        // Verify cache was not cleared
        verify(rallyProcessorCacheEvictor, never()).evictCache(anyString(), anyString());
    }

    @Test
    public void testSignalJobCompletionWhenAllJobsComplete() {
        // Execute all job completions
        cacheClearingMechanism.signalJobCompletion();
        cacheClearingMechanism.signalJobCompletion();

        // Verify cache was cleared for all required caches
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_KPI_DATA);
    }

    @Test
    public void testSetJobCount() {
        // Set new job count
        cacheClearingMechanism.setJobCount(3);

        // Execute two job completions (not all jobs complete)
        cacheClearingMechanism.signalJobCompletion();
        cacheClearingMechanism.signalJobCompletion();

        // Verify cache was not cleared
        verify(rallyProcessorCacheEvictor, never()).evictCache(anyString(), anyString());

        // Execute final job completion
        cacheClearingMechanism.signalJobCompletion();

        // Verify cache was cleared
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_ACCOUNT_HIERARCHY);
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
        verify(rallyProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_KPI_DATA);
    }
}
