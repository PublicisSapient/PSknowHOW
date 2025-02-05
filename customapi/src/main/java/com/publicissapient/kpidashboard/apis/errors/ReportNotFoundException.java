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

package com.publicissapient.kpidashboard.apis.errors;

/**
 * Exception thrown when a report is not found.
 * <p>
 * This exception is used to indicate that an attempt to retrieve a report
 * has failed because the report does not exist.
 * </p>
 * @Author girpatha
 */
public class ReportNotFoundException extends  RuntimeException {
    /**
     * Constructs a new ReportNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ReportNotFoundException(String message) {
        super(message);
    }
}
