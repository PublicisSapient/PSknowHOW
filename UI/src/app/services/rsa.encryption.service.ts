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
import { JSEncrypt } from 'jsencrypt';

@Injectable({
    providedIn: 'root'
})

export class RsaEncryptionService {

    PUBLIC_KEY = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvyqn4b2FDrDIcRrP0yBA1Ffu9FjJPfvNIym5zIQQeu9oA0RH+ILUSPysYkrbmWrEvH05L07F6wddiW/pTRl5AanheBQxX0xq87QgBTJ8EJUTvjBP2LcHXjvxzFsGLymb2PoK3G6/9O5LNEtQUOvTRyvrwxTlio35gvkvevFndItVfRuCVC1jX3WqLWlJ9C1Tiemp6Wk2roZ74RKEtnbZwHTu1MGrE8ijD64P53yM0qvGYjTbthQ/GnHuMgONXuGiIRijY888TcV6KyzVCxzmCVHlcvPSz2RuPiU6rL4J7MsBMF1xwo/oTp9bhBabe2v9DpzRjdbrYOjFDFywxUgvDwIDAQAB';

    encrypt(plainText) {
        const jsencrypt = new JSEncrypt({ default_key_size: '2048' });
        jsencrypt.setPublicKey(this.PUBLIC_KEY);
        const encryptedText = jsencrypt.encrypt(plainText);

        return encryptedText;
    }

}
