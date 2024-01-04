import { Injectable } from '@angular/core';
import { ScriptStore } from './script-store';

declare let document: any;

@Injectable({
  providedIn: 'root'
})
export class GoogleAnalyticsService {
  window = window;
  private scripts: any = {};

  constructor() {
    ScriptStore.forEach((script: any) => {
      this.scripts[script.name] = {
        loaded: false,
        src: script.src
      };
    });
  }


  load(...scripts: string[]) {
    const promises: any[] = [];
    scripts.forEach((script) => promises.push(this.loadScript(script)));
    return Promise.all(promises);
  }

  loadScript(name: string) {
    return new Promise((resolve, reject) => {
      //resolve if already loaded
      if (this.scripts[name].loaded) {
        resolve({ script: name, loaded: true, status: 'Already Loaded' });
      } else {
        //load script
        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = this.scripts[name].src;
        if (script.readyState) {  //IE
          script.onreadystatechange = () => {
            if (script.readyState === 'loaded' || script.readyState === 'complete') {
              script.onreadystatechange = null;
              this.scripts[name].loaded = true;
              resolve({ script: name, loaded: true, status: 'Loaded' });
            }
          };
        } else {  //Others
          script.onload = () => {
            this.scripts[name].loaded = true;
            resolve({ script: name, loaded: true, status: 'Loaded' });
          };
        }
        script.onerror = (error: any) => resolve({ script: name, loaded: false, status: 'Loaded' });
        document.getElementsByTagName('head')[0].appendChild(script);
      }
    });
  }

  setPageLoad(data) {
    const dataLayer = this.window && this.window.hasOwnProperty('dataLayer') ? this.window['dataLayer'] : [];

    dataLayer.push({
      event: 'pageLoad',
      pageName: data.url,
      userRole: data.userRole,
      server: {
        instanceName: window.location.origin,
        version: data.version
      }
    });
  }

  setLoginMethod(data, loginType){
    const dataLayer = this.window && typeof this.window['dataLayer'] !== undefined ? this.window['dataLayer'] : [];
    dataLayer.push({
         'event' : 'login',
         'authentication_method' : loginType,
         'user_id' : data.user_id 
    });
  }

  setProjectData(data) {
    const dataLayer = this.window && typeof this.window['dataLayer'] !== undefined ? this.window['dataLayer'] : [];
    for (let i = 0; i<data.length; i++) {
      dataLayer.push({
        event: 'ProjectViewed',
        ...data[i]
      });
    }
  }

  setProjectToolsData(data){
    const dataLayer = this.window && typeof this.window['dataLayer'] !== undefined ? this.window['dataLayer'] : [];
    dataLayer?.push({
      event: 'ProjectToolsConfigured',
      ...data
    });
  }

  setKpiData(data){
    const dataLayer = this.window && typeof this.window['dataLayer'] !== undefined ? this.window['dataLayer'] : [];
    dataLayer?.push({
      event: 'kpiViewed',
      ...data
    });
  }
}
