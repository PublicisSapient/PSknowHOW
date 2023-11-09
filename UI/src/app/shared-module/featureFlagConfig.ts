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

// This file contains the feature flag configuration 
// Available roles are 'superAdmin', 'projectAdmin', 'projectViewer', 'roleViewer'
// Available personas are none as of now

export const features = [
    // to test directive
    {
        'name': 'Developer',
        'enabled': true
    },
    // to test route guard
    {
        'name': 'Config',
        'enabled': true
    },
    // to test child module
    {
        'name': 'Profile',
        'enabled': true
    },
    {
        'name': 'GrantRequests',
        'enabled': true
    }
];