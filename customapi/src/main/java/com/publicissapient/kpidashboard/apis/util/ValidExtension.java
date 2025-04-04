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

package com.publicissapient.kpidashboard.apis.util;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/** The interface Valid extension. */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = FileExtensionValidator.class)
@Documented
public @interface ValidExtension {

	/**
	 * Message string.
	 *
	 * @return the string
	 */
	String message() default "Not a valid image";

	/**
	 * Groups class [ ].
	 *
	 * @return the class [ ]
	 */
	Class<?>[] groups() default {};

	/**
	 * Payload class [ ].
	 *
	 * @return the class [ ]
	 */
	Class<? extends Payload>[] payload() default {};
}
