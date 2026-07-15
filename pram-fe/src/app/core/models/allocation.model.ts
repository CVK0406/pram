export interface Allocation {
  allocationId?: number;
  employeeId: number;
  employeeName: string;
  projectId: number;
  projectCode: string;
  allocationPercent: number;
  roleInProject: string;
  startDate: string;
  endDate: string;
}

export interface AllocationRequest {
  employeeId: number;
  projectId: number;
  allocationPercent: number;
  roleInProject: string;
  startDate: string;
  endDate?: string;
}
