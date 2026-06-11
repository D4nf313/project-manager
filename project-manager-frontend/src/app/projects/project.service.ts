import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project, CreateProjectRequest } from '../core/models/models';
import { environment } from '../../environments/environment';

const API = `${environment.apiUrl}/api`;


@Injectable({ providedIn: 'root' })
export class ProjectService {

  constructor(private http: HttpClient) {}

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${API}/projects`);
  }

  createProject(body: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(`${API}/projects`, body);
  }
}
