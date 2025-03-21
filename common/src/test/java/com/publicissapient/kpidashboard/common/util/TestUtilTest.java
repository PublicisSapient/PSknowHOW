/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

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
