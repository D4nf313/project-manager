import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  TokenRequest,
  TokenResponse,
  SessionData,
  WorkspaceInfo,
} from '../core/models/models';

const API = 'http://localhost:8080/api';
const SESSION_KEY = 'pm_session';
const TEMP_KEY = 'pm_temp';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // ── Estado reactivo con Signals ───────────────────────────
  private _session = signal<SessionData | null>(this.loadSession());

  readonly session = this._session.asReadonly();
  readonly isLoggedIn = computed(() => !!this._session());
  readonly role = computed(() => this._session()?.role ?? null);
  readonly userName = computed(() => this._session()?.userName ?? '');
  readonly workspaceName = computed(() => this._session()?.workspaceName ?? '');

  // ── Estado temporal del flujo de login ────────────────────
  private _tempToken = signal<string | null>(null);
  private _workspaces = signal<WorkspaceInfo[]>([]);

  readonly workspaces = this._workspaces.asReadonly();

  constructor(private http: HttpClient) {}

  // ── Paso 1: Login ─────────────────────────────────────────
  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, body).pipe(
      tap((res) => {
        this._tempToken.set(res.tempToken);
        this._workspaces.set(res.workspaces);
        sessionStorage.setItem(
          TEMP_KEY,
          JSON.stringify({
            tempToken: res.tempToken,
            workspaces: res.workspaces,
            userName: res.name,
            userEmail: res.email,
          }),
        );
      }),
    );
  }

  // ── Paso 2: Selección de workspace ────────────────────────
  selectWorkspace(workspaceId: string): Observable<TokenResponse> {
    const tempToken = this.getTempToken();
    const headers = new HttpHeaders({ Authorization: `Bearer ${tempToken}` });
    const body: TokenRequest = { workspaceId };

    return this.http
      .post<TokenResponse>(`${API}/auth/token`, body, { headers })
      .pipe(
        tap((res) => {
          const temp = this.getTempData();
          const session: SessionData = {
            token: res.token,
            workspaceId: res.workspaceId,
            workspaceName: res.workspaceName,
            role: res.role,
            userName: temp?.userName ?? '',
            userEmail: temp?.userEmail ?? '',
          };
          this.saveSession(session);
          // ← NO borrar TEMP_KEY, solo actualizar sin el tempToken
          sessionStorage.setItem(
            TEMP_KEY,
            JSON.stringify({
              ...temp,
              tempToken: temp?.tempToken, // mantener el tempToken original
            }),
          );
        }),
      );
  }

  // ── Logout ───────────────────────────────────────────────
  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    sessionStorage.removeItem(TEMP_KEY);
    this._session.set(null);
    this._tempToken.set(null);
    this._workspaces.set([]);
  }

  // ── Helpers privados ─────────────────────────────────────
  private saveSession(session: SessionData): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this._session.set(session);
  }

  private loadSession(): SessionData | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private getTempToken(): string {
    const temp = this.getTempData();
    return temp?.tempToken ?? this._tempToken() ?? '';
  }

  private getTempData(): any {
    try {
      const raw = sessionStorage.getItem(TEMP_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  // ── Para el guard ─────────────────────────────────────────
  getToken(): string | null {
    return this._session()?.token ?? null;
  }

  // ── Para recuperar workspaces tras recarga ────────────────
  loadWorkspacesFromSession(): WorkspaceInfo[] {
    const temp = this.getTempData();
    if (temp?.workspaces) {
      this._workspaces.set(temp.workspaces);
      this._tempToken.set(temp.tempToken);
      return temp.workspaces;
    }
    return this._workspaces();
  }
  clearSession(): void {
    localStorage.removeItem(SESSION_KEY);
    this._session.set(null);
  }
}
