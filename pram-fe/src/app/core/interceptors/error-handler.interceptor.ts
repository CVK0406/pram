import { Injectable, inject } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class ErrorHandlerInterceptor implements HttpInterceptor {
  private snackBar = inject(MatSnackBar);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        // Only handle 500+ and network errors — 400/409 handled per-component
        if (err.status === 0) {
          this.snackBar.open('Network error — check your connection', 'Close', { duration: 6000 });
        } else if (err.status >= 500) {
          const msg = err.error?.message || 'Server error — please try again later';
          this.snackBar.open(msg, 'Close', { duration: 6000 });
        }
        return throwError(() => err);
      }),
    );
  }
}
