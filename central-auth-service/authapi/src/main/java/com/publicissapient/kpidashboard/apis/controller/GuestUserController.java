/*
 *  Copyright 2024 <Sapient Corporation>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package com.publicissapient.kpidashboard.apis.controller;

import com.publicissapient.kpidashboard.apis.service.GuestUserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@AllArgsConstructor
@Slf4j
public class GuestUserController {

    private final GuestUserService guestUserService;

    @GetMapping("/guest-login")
    public RedirectView guestLogin(
            @RequestParam String displayName,
            @RequestParam String redirectUri,
            @NotNull HttpServletResponse response
    ) {
        guestUserService.loginUserAsGuest(displayName, response);

        return new RedirectView(redirectUri);
    }
}
