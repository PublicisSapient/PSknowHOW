import { TestBed } from '@angular/core/testing';
import { AppModule } from './app.module';
import { AppInitializerService } from './services/app-initializer.service';
import { APP_INITIALIZER } from '@angular/core';

describe('AppModule', () => {
  let module: AppModule;
  let appInitializerService: AppInitializerService;

  beforeEach(async () => {
    const appInitializerServiceSpy = jasmine.createSpyObj(
      'appInitializerService',
      ['checkFeatureFlag'],
    );
    module = new AppModule();
    TestBed.configureTestingModule({
      imports: [AppModule],
      providers: [
        { provide: AppInitializerService, useValue: appInitializerServiceSpy },
      ],
    });
    appInitializerService = TestBed.inject(AppInitializerService);
  });

  it('should create the app', () => {
    expect(module).toBeTruthy();
  });

  it('should provide APP_INITIALIZER with the correct factory', () => {
    const appInitializer = TestBed.inject(APP_INITIALIZER);
    expect(appInitializer).toBeDefined();
  });
});
