export interface Employee {
  employeeId?: number;
  employeeCode: string;
  fullName: string;
  email: string;
  role: string;
  department: string;
}

export interface EmployeeRequest {
  employeeCode: string;
  fullName: string;
  email: string;
  role: string;
  department: string;
}
