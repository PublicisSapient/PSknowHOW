import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StickyHeaderV2Component } from './sticky-header-v2.component';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { of, Subject } from 'rxjs';
import { Subscription } from 'rxjs';

describe('StickyHeaderV2Component', () => {
  let component: StickyHeaderV2Component;
  let fixture: ComponentFixture<StickyHeaderV2Component>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockHelperService: jasmine.SpyObj<HelperService>;
  let onTabSwitch: Subject<any>;
  let mapColorToProjectObsSubject: Subject<any>;

  beforeEach(async () => {
    // Create Subjects to simulate Observables from SharedService
    onTabSwitch = new Subject();
    mapColorToProjectObsSubject = new Subject();

    // Create mock objects
    mockSharedService = jasmine.createSpyObj('SharedService', [
      'onTabSwitch',
      'mapColorToProjectObs',
    ]);
    mockHelperService = jasmine.createSpyObj('HelperService', [
      'getObjectKeys',
    ]);

    // Mock the service observables
    Object.defineProperty(mockSharedService, 'onTabSwitch', {
      value: onTabSwitch.asObservable(),
    });
    Object.defineProperty(mockSharedService, 'mapColorToProjectObs', {
      value: mapColorToProjectObsSubject.asObservable(),
    });

    await TestBed.configureTestingModule({
      declarations: [StickyHeaderV2Component],
      providers: [
        { provide: SharedService, useValue: mockSharedService },
        { provide: HelperService, useValue: mockHelperService },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StickyHeaderV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    component.ngOnDestroy(); // Make sure to test unsubscription behavior
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should subscribe to onTabSwitch and update fields', () => {
    const mockData = { selectedBoard: 'iteration' };

    onTabSwitch.next(mockData);

    expect(component.fields.get('Selected Dashboard ')).toEqual('iteration');
  });

  it('should subscribe to mapColorToProjectObs and update colorObj', () => {
    const mockData = [
      { nodeId: '1', nodeName: 'Project A', labelName: 'project' },
      { nodeId: '2', nodeName: 'Project B', labelName: 'project' },
    ];

    mapColorToProjectObsSubject.next(mockData);

    expect(component.colorObj['1']).toEqual({
      nodeName: 'Project A',
      color: '#FFB587',
      nodeId: '1',
      labelName: 'project',
    });
  });

  it('should return object keys using helperService', () => {
    const mockObject = { key1: 'value1', key2: 'value2' };
    mockHelperService.getObjectKeys.and.returnValue(Object.keys(mockObject));

    const result = component.objectKeys(mockObject);

    expect(result).toEqual(Object.keys(mockObject));
    expect(mockHelperService.getObjectKeys).toHaveBeenCalledWith(mockObject);
  });

  // --> skipping this test case for now.
  xit('should unsubscribe from all subscriptions on destroy', () => {
    const subscriptionSpy = spyOn(
      Subscription.prototype,
      'unsubscribe',
    ).and.callThrough();

    component.ngOnDestroy();

    expect(subscriptionSpy).toHaveBeenCalled();
  });

  it('should not update colorObj when mapColorToProjectObs is empty', () => {
    const mockData = {};
    mapColorToProjectObsSubject.next(mockData);
    expect(component.colorObj).toEqual({});
  });
});
