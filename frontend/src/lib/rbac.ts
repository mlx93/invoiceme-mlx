import { UserRole } from '@/types/user';

export function canCreateCustomer(role: UserRole): boolean {
  return ['SYSADMIN', 'ACCOUNTANT', 'SALES'].includes(role);
}

export function canEditCustomer(role: UserRole): boolean {
  return ['SYSADMIN', 'ACCOUNTANT'].includes(role);
}

export function canDeleteCustomer(role: UserRole): boolean {
  return role === 'SYSADMIN';
}

export function canCreateInvoice(role: UserRole): boolean {
  return ['SYSADMIN', 'ACCOUNTANT', 'SALES'].includes(role);
}

export function canEditInvoice(role: UserRole, invoiceStatus: string): boolean {
  if (role === 'SYSADMIN') {
    return invoiceStatus === 'DRAFT' || invoiceStatus === 'SENT';
  }
  if (['ACCOUNTANT', 'SALES'].includes(role)) {
    return invoiceStatus === 'DRAFT';
  }
  return false;
}

export function canCancelInvoice(role: UserRole): boolean {
  return role === 'SYSADMIN';
}

export function canRecordPayment(role: UserRole, invoiceCustomerId?: string, userCustomerId?: string): boolean {
  if (['SYSADMIN', 'ACCOUNTANT'].includes(role)) {
    return true;
  }
  if (role === 'CUSTOMER') {
    return invoiceCustomerId === userCustomerId;
  }
  return false;
}

export function canIssueRefund(role: UserRole): boolean {
  return role === 'SYSADMIN';
}

export function canViewDashboard(role: UserRole): boolean {
  return ['SYSADMIN', 'ACCOUNTANT', 'CUSTOMER'].includes(role);
}

export function canApproveUsers(role: UserRole): boolean {
  return role === 'SYSADMIN';
}

