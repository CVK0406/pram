export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'COMPLETED';

export interface Project {
  projectId?: number;
  projectCode: string;
  projectName: string;
  customer: string;
  startDate: string;
  endDate: string;
  status: ProjectStatus;
}

export interface ProjectRequest {
  projectCode: string;
  projectName: string;
  customer: string;
  startDate: string;
  endDate: string;
  status?: ProjectStatus;
}

export interface ProjectStatusRequest {
  status: ProjectStatus;
}
