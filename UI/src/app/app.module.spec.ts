
import { AppModule } from './app.module';

describe('AppModule', () => {
  let module: AppModule;

  beforeEach(async () => {
    module = new AppModule();
  });

  it('should create the app', () => {
    expect(module).toBeTruthy();
  });
});
