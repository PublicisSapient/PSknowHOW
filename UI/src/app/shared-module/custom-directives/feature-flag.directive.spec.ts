/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { TestBed, ComponentFixture } from '@angular/core/testing';
import {
  Component,
  Directive,
  Input,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { FeatureFlagDirective } from './feature-flag.directive';
import { FeatureFlagsService } from '../../services/feature-toggle.service';
import { of } from 'rxjs';

@Component({
  template: `<ng-template [featureFlag]="'testFeature'"
    >Feature Content</ng-template
  >`,
})
class TestComponent {}

describe('FeatureFlagDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let featureFlagsService: jasmine.SpyObj<FeatureFlagsService>;

  beforeEach(() => {
    const featureFlagsSpy = jasmine.createSpyObj('FeatureFlagsService', [
      'isFeatureEnabled',
    ]);

    TestBed.configureTestingModule({
      declarations: [FeatureFlagDirective, TestComponent],
      providers: [{ provide: FeatureFlagsService, useValue: featureFlagsSpy }],
    });

    fixture = TestBed.createComponent(TestComponent);
    featureFlagsService = TestBed.inject(
      FeatureFlagsService,
    ) as jasmine.SpyObj<FeatureFlagsService>;
  });

  it('should display content when feature is enabled', async () => {
    featureFlagsService.isFeatureEnabled.and.returnValue(Promise.resolve(true));

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Feature Content');
  });

  it('should not display content when feature is disabled', async () => {
    featureFlagsService.isFeatureEnabled.and.returnValue(
      Promise.resolve(false),
    );

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Feature Content');
  });
});
