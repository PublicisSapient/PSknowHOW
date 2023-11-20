import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from '../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { VerifyComponent } from './verify.component';

import { IterationComponent } from 'src/app/dashboard/iteration/iteration.component';
import { PageNotFoundComponent } from 'src/app/page-not-found/page-not-found.component';

describe('VerifyComponent', () => {
  let component: VerifyComponent;
  let fixture: ComponentFixture<VerifyComponent>;

  beforeEach(async () => {

    const routes: Routes = [
      { path: 'dashboard/iteration', component: IterationComponent },
      { path: 'pageNotFound', component: PageNotFoundComponent }
    ];

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [VerifyComponent],
      providers: [
        HttpService, SharedService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(VerifyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
