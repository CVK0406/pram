import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private http = inject(HttpClient);

  ngOnInit(): void {
    console.log('App initialized, testing backend connection...');
    this.http.get(`${environment.apiBaseUrl}/employees`).subscribe({
      next: (data) => console.log('Connected to backend:', data),
      error: (err) => console.error('Backend connection failed:', err),
    });
  }
}
