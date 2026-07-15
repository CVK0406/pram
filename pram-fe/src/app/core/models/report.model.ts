export interface EmployeeUtilization {
  employeeId: number;
  employeeCode: string;
  fullName: string;
  totalAllocation: number;
}

export interface AvailableResource {
  employeeId: number;
  fullName: string;
  role: string;
  available: number;
}

export interface OverloadedEmployee {
  employeeId: number;
  fullName: string;
  totalAllocation: number;
}
