 import { Injectable } from '@angular/core';
 import { CanActivate } from '@angular/router';
 import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
 import { TextEncryptionService } from './text.encryption.service';
 import { SharedService } from './shared.service';
 @Injectable()
 export class GuestGuard implements CanActivate {

     constructor(private aesEncryption: TextEncryptionService,private sharedService : SharedService) { }

     canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const decryptedText = this.aesEncryption.convertText(localStorage.getItem('authorities'), 'decrypt');
        if(!decryptedText) {
            return false;
        }
        if (JSON.parse(decryptedText).includes('ROLE_GUEST')) {
             return false;
        }else{
             return true;
        }
     }
 }
