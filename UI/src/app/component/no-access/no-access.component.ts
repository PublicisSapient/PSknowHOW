import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';

@Component({
    selector: 'app-no-access',
    templateUrl: './no-access.component.html',
    styleUrls: ['./no-access.component.css']
})
export class NoAccessComponent {

    @Input() selectedTab: string;
    @Input() userEnable: boolean;
    isSuperAdmin = false;
    constructor( private getAuthorizationService: GetAuthorizationService){}
    ngOnInit() {
        if (this.getAuthorizationService.checkIfSuperUser()) {
            this.isSuperAdmin = true;
        }
    }
}
