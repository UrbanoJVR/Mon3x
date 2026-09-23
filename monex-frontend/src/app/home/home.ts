import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { Message } from 'primeng/message';

interface HealthResponse {
  status: string;
  application: string;
}

@Component({
  selector: 'app-home',
  imports: [Card, Tag, Message],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  private readonly http = inject(HttpClient);

  readonly loading = signal(true);
  readonly health = signal<HealthResponse | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.http.get<HealthResponse>('/api/health').subscribe({
      next: (response) => {
        this.health.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(
          'No se pudo contactar con el backend. Arranca monex-backend o usa el proxy de desarrollo.',
        );
        this.loading.set(false);
      },
    });
  }
}
