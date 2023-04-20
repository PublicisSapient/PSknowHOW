 import { Injectable } from '@angular/core';
 import * as CryptoJS from 'crypto-js';

 @Injectable({
     providedIn: 'root'
 })


 export class TextEncryptionService {
    enKey = '6v9y$B&E';
    convertText(plainText: string, conversion: string) {
        let conversionOutput;

        if(!plainText) {
            return;
        }
        if (conversion=='encrypt') {
            conversionOutput = CryptoJS.AES.encrypt(plainText.trim(), this.enKey.trim()).toString();
        } else {
            conversionOutput = CryptoJS.AES.decrypt(plainText.trim(), this.enKey.trim()).toString(CryptoJS.enc.Utf8);
        }

        return conversionOutput;
    }

 }
