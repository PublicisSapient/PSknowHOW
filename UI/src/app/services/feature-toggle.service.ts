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

  async loadConfig() {
    return await this.http.getFeatureFlags();
  }

  async isFeatureEnabled(key: string) {
    if (this.config) {
      this.config = features.concat(this.config);
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
    } else {
      this.config = this.loadConfig();
      this.config = features.concat(this.config);
      return this.isFeatureEnabled(key);
    }
  }
}
