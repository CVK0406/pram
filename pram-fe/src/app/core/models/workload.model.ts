export interface WorkloadAllocation {
  projectCode: string;
  allocationPercent: number;
  roleInProject: string;
}

export interface EmployeeWorkload {
  employeeId: number;
  employeeName: string;
  totalAllocation: number;
  available: number;
  allocations: WorkloadAllocation[];
}
