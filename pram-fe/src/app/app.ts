import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected readonly title = signal('pram-frontend');
  private http = inject(HttpClient);

  ngOnInit(): void {
    console.log('App initialized, testing backend connection...');
    this.http.get(`${environment.apiBaseUrl}/employees`).subscribe({
      next: (data) => {
        console.log('Successfully connected to backend! Employees data:', data);
      },
      error: (error) => {
        console.error('Failed to connect to backend:', error);
      }
    });
  }
}
