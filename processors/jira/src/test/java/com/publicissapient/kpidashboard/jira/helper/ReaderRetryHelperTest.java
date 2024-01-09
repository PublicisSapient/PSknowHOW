package com.publicissapient.kpidashboard.jira.helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ReaderRetryHelperTest {

	ReaderRetryHelper.RetryableOperation<Double> operation = () -> Math.sqrt(49);

	@InjectMocks
	ReaderRetryHelper readerRetryHelper;

	@Test
	public void executeWithRetryTest() throws Exception {
		assertEquals(Double.valueOf(7.0), readerRetryHelper.executeWithRetry(operation));
	}

	@Test(expected = NullPointerException.class)
	public void executeWithRetryNullTest() throws Exception {
		readerRetryHelper.executeWithRetry(null);
	}
}
