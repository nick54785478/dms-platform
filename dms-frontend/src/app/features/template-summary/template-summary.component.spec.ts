import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateSummaryComponent } from './template-summary.component';

describe('TemplateSummaryComponent', () => {
  let component: TemplateSummaryComponent;
  let fixture: ComponentFixture<TemplateSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemplateSummaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TemplateSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
