import { Component, TemplateRef, ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FeatureFlagDirective } from './feature-flag.directive';
import { FeatureFlagsService } from '../../services/feature-toggle.service';
import { SharedService } from 'src/app/services/shared.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { HttpService } from 'src/app/services/http.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';

describe('FeatureFlagDirective', () => {
    let directive: FeatureFlagDirective;
    let tpl: TemplateRef<any>;
    let vcr: ViewContainerRef;
    let featureFlagService: FeatureFlagsService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            declarations: [FeatureFlagDirective],
            providers: [
                FeatureFlagsService,
                SharedService,
                GetAuthorizationService,
                HttpService,
                { provide: APP_CONFIG, useValue: AppConfig }
            ]
        }).compileComponents();

        tpl = {} as TemplateRef<any>;
        vcr = { createEmbeddedView: jasmine.createSpy('createEmbeddedView'), clear: jasmine.createSpy('clear') } as unknown as ViewContainerRef;
        // featureFlagService = { isFeatureEnabled: jasmine.createSpy('isFeatureEnabled').and.returnValue(of(true)) } as unknown as FeatureFlagsService;
        featureFlagService = TestBed.inject(FeatureFlagsService);
        directive = new FeatureFlagDirective(tpl, vcr, featureFlagService);
    });

    /** TODO: isFeatureEnabled should return a promise */
    xit('should create an embedded view when feature is enabled', () => {
        let featureEnabledSpy = spyOn<any>(featureFlagService, 'isFeatureEnabled').and.returnValue(of(false));
        directive.featureFlag = 'myFeature';
        expect(featureEnabledSpy).toHaveBeenCalledWith('myFeature');
        expect(vcr.createEmbeddedView).toHaveBeenCalledWith(tpl);
        expect(vcr.clear).not.toHaveBeenCalled();
    });

    xit('should clear the view container when feature is disabled', () => {
        let featureEnabledSpy = spyOn<any>(featureFlagService, 'isFeatureEnabled').and.returnValue(of(false));

        directive.featureFlag = 'myFeature';

        expect(featureEnabledSpy).toHaveBeenCalledWith('myFeature');
        expect(vcr.clear).toHaveBeenCalled();
        expect(vcr.createEmbeddedView).not.toHaveBeenCalled();
    });
});
