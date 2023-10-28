import { Injectable } from '@angular/core';
import { features } from '../shared-module/featureFlagConfig';
import { GetAuthorizationService } from './get-authorization.service';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagsService {
  config= null;

  constructor(private roleService: GetAuthorizationService) { }

  loadConfig() {
    this.config = features;
    return this.config;
  }

  isFeatureEnabled(key: string) {
    if (this.config.length) {
      let requiredConfig = this.config.filter(feature => feature['featureName'].toLowerCase() === key.toLowerCase())[0];
      if (requiredConfig) {
        if(requiredConfig.enabled && requiredConfig.roles.includes(this.roleService.getRole())) {
          return true;
        } else {
          return false;
        }
      } else {
        return true;
      }
    }
    return true;
  }
}
