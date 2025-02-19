import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportContainerComponent } from './report-container.component';

describe('ReportContainerComponent', () => {
  let component: ReportContainerComponent;
  let fixture: ComponentFixture<ReportContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ReportContainerComponent.getkpiwidth() getkpiwidth method', () => {
    describe('Happy Paths', () => {
      it('should return "p-col-12" for a KPI width of 100', () => {
        // Test to ensure the method returns the correct CSS class for 100% width
        const result = component.getkpiwidth(100);
        expect(result).toBe('p-col-12');
      });
  
      it('should return "p-col-6" for a KPI width of 50', () => {
        // Test to ensure the method returns the correct CSS class for 50% width
        const result = component.getkpiwidth(50);
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-8" for a KPI width of 66', () => {
        // Test to ensure the method returns the correct CSS class for 66% width
        const result = component.getkpiwidth(66);
        expect(result).toBe('p-col-8');
      });
  
      it('should return "p-col-4" for a KPI width of 33', () => {
        // Test to ensure the method returns the correct CSS class for 33% width
        const result = component.getkpiwidth(33);
        expect(result).toBe('p-col-4');
      });
    });
  
    describe('Edge Cases', () => {
      it('should return "p-col-6" for an unrecognized KPI width', () => {
        // Test to ensure the method defaults to "p-col-6" for an unrecognized width
        const result = component.getkpiwidth(75);
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-6" for a KPI width of 0', () => {
        // Test to ensure the method defaults to "p-col-6" for a width of 0
        const result = component.getkpiwidth(0);
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-6" for a negative KPI width', () => {
        // Test to ensure the method defaults to "p-col-6" for a negative width
        const result = component.getkpiwidth(-50);
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-6" for a non-numeric KPI width', () => {
        // Test to ensure the method defaults to "p-col-6" for a non-numeric width
        const result = component.getkpiwidth('abc');
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-6" for a null KPI width', () => {
        // Test to ensure the method defaults to "p-col-6" for a null width
        const result = component.getkpiwidth(null);
        expect(result).toBe('p-col-6');
      });
  
      it('should return "p-col-6" for an undefined KPI width', () => {
        // Test to ensure the method defaults to "p-col-6" for an undefined width
        const result = component.getkpiwidth(undefined);
        expect(result).toBe('p-col-6');
      });
    });
  });
});
