package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Test;

public class TestUtilTest {

	@Test
	public void testConvertObjectToJsonBytes() throws IOException {
		// Create a sample object
		SampleObject sampleObject = new SampleObject("TestName", 25);

		// Convert the object to JSON bytes
		byte[] jsonBytes = TestUtil.convertObjectToJsonBytes(sampleObject);

		// Verify the generated JSON bytes
		byte[] expectedJsonBytes = "{\"name\":\"TestName\",\"age\":25}".getBytes();
		assertArrayEquals(expectedJsonBytes, jsonBytes);
	}

	// Sample object for testing
	private static class SampleObject {
		private String name;
		private int age;

		// Default constructor is needed for Jackson
		public SampleObject() {
		}

		public SampleObject(String name, int age) {
			this.name = name;
			this.age = age;
		}

		// Getter methods are needed for Jackson
		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		// Setter methods (if needed)

		@Override
		public String toString() {
			return "SampleObject{" + "name='" + name + '\'' + ", age=" + age + '}';
		}
	}
}
