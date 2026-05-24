// ── Auth ──────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface WorkspaceInfo {
  id: string;
  name: string;
  role: 'ADMIN' | 'EDITOR' | 'LECTOR';
}

export interface LoginResponse {
  tempToken: string;
  userId: string;
  name: string;
  email: string;
  workspaces: WorkspaceInfo[];
}

export interface TokenRequest {
  workspaceId: string;
}

export interface TokenResponse {
  token: string;
  workspaceId: string;
  workspaceName: string;
  role: 'ADMIN' | 'EDITOR' | 'LECTOR';
}

// ── Session (estado que se guarda en localStorage) ────────────

export interface SessionData {
  token: string;
  workspaceId: string;
  workspaceName: string;
  role: 'ADMIN' | 'EDITOR' | 'LECTOR';
  userName: string;
  userEmail: string;
}

// ── Projects ──────────────────────────────────────────────────

export interface Project {
  id: string;
  name: string;
  description: string;
  workspaceId: string;
  workspaceName: string;
  createdAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description: string;
}

// ── UI helpers ────────────────────────────────────────────────

export type UserRole = 'ADMIN' | 'EDITOR' | 'LECTOR';

export function canCreateProjects(role: UserRole): boolean {
  return role === 'ADMIN' || role === 'EDITOR';
}
