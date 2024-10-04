import { Component, TemplateRef, ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FeatureFlagDirective } from './feature-flag.directive';
import { AppConfig, APP_CONFIG } from 'src/app/core/configs/app.config';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { FeatureFlagsService } from 'src/app/core/services/feature-toggle.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { GetAuthorizationService } from 'src/app/core/services/get-authorization.service';
import { HttpService } from 'src/app/core/services/http.service';

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
