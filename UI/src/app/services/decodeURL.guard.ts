import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';

@Injectable({
    providedIn: 'root',
})
export class DecodeUrlGuard implements CanActivate {
    constructor(private router: Router) { }

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): boolean | UrlTree {
        // Decode the full URL
        const decodedUrl = decodeURIComponent(state.url);

        // Compare decoded URL with the current state URL
        if (state.url.indexOf('stateFilters') != -1 && state.url.indexOf('?stateFilters=') == -1) {
            // If the decoded URL differs, navigate to the decoded URL
            this.router.navigateByUrl(decodedUrl, { replaceUrl: true });
            return false; // Prevent further navigation until the URL is corrected
        }

        // Allow navigation
        return true;
    }
}