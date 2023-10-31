import { Injectable } from '@angular/core';
import { features } from '../shared-module/featureFlagConfig';
import { GetAuthorizationService } from './get-authorization.service';
import { HttpService } from './http.service';

@Injectable({
  providedIn: 'root'
})
export class FeatureFlagsService {
  config = null;

  constructor(private roleService: GetAuthorizationService, private http: HttpService) { }

  loadConfig() {
    this.http.getFeatureFlags().subscribe(response => {
      this.config = features.concat(response);
      return this.config;
    });
  }

  isFeatureEnabled(key: string) {
    if (this.config.length) {
      let requiredConfig = this.config.filter(feature => feature['name'].toLowerCase() === key.toLowerCase())[0];
      if (requiredConfig) {
        if (requiredConfig.enabled) {
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
