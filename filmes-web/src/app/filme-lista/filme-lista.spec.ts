import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FilmeLista } from './filme-lista';

describe('FilmeLista', () => {
  let component: FilmeLista;
  let fixture: ComponentFixture<FilmeLista>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilmeLista],
    }).compileComponents();

    fixture = TestBed.createComponent(FilmeLista);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
