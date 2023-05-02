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


import { Injectable } from '@angular/core';
import { SharedService } from './shared.service';

@Injectable()
export class GetAuthService {
    stopListening: Function;
    private authenthicate;

    constructor(private sharedService : SharedService){}

    checkAuth() {
        const user_name = this.getToken();
        if (user_name !== 'null' && user_name !== null && user_name !== undefined) {
            this.authenthicate = true;
        } else {
            // redirect to login page
            this.authenthicate = false;
        }
        return this.authenthicate;

    }

    getToken() {
        return this.sharedService.getCurrentUserDetails('authorities');
    }
}

