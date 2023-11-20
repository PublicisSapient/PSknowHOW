import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChild, CanLoad, Route, Router, RouterStateSnapshot, UrlSegment, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { FeatureFlagsService } from './feature-toggle.service';

@Injectable({
    providedIn: 'root',
})

export class FeatureGuard implements CanLoad, CanActivateChild {
    constructor(
        private featureFlagsService: FeatureFlagsService,
        private router: Router
    ) { }

    canLoad(
        route: Route,
        segments: UrlSegment[]
    ):
        | Observable<boolean | UrlTree>
        | Promise<boolean | UrlTree>
        | boolean
        | UrlTree {
        const {
            data: { feature }, // <-- Get the module name from route data
        } = route;
        if (feature) {
            const isEnabled = this.featureFlagsService.isFeatureEnabled(feature);
            if (isEnabled) {
                return true;
            }
        } else {
            return true;
        }
        this.router.navigate(['/']);
        return false;
    }

    canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
        const {
            data: { feature }, // <-- Get the module name from route data
        } = childRoute;
        if (feature) {
            const isEnabled = this.featureFlagsService.isFeatureEnabled(feature);
            if (isEnabled) {
                return true;
            }
        } else {
            return true;
        }
        this.router.navigate(['/']);
        return false;
    }
}
