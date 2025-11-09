'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useAuth } from '@/contexts/AuthContext';
import { useInvoices } from '@/hooks/useInvoices';
import { useRecordPayment } from '@/hooks/usePayments';
import { useCustomer } from '@/hooks/useCustomers';
import { PaymentForm } from '@/components/payments/PaymentForm';
import { formatCurrency, formatDate } from '@/lib/utils';
import { InvoiceStatus } from '@/types/common';

export default function CustomerPortalPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string | null>(null);
  const [selectedBalanceDue, setSelectedBalanceDue] = useState<number>(0);

  // Get customer data if user has customerId
  const { customer } = useCustomer(user?.customerId || null);

  // Get invoices for this customer only
  const { invoices, loading: invoicesLoading, refetch: refetchInvoices } = useInvoices({
    size: 50,
    customerId: user?.customerId,
    status: statusFilter || undefined,
  });

  const { recordPayment } = useRecordPayment();

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && user.role !== 'CUSTOMER') {
      router.push('/dashboard');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const handlePaymentRecorded = () => {
    setPaymentDialogOpen(false);
    setSelectedInvoiceId(null);
    refetchInvoices();
  };

  const handlePayNow = (invoiceId: string, balanceDue: number) => {
    setSelectedInvoiceId(invoiceId);
    setSelectedBalanceDue(balanceDue);
    setPaymentDialogOpen(true);
  };

  const getStatusBadgeVariant = (status: InvoiceStatus) => {
    switch (status) {
      case 'PAID':
        return 'default';
      case 'OVERDUE':
        return 'destructive';
      default:
        return 'outline';
    }
  };

  // Calculate summary metrics
  const outstandingBalance = invoices.reduce((sum, inv) => sum + inv.balanceDue.amount, 0);
  const unpaidCount = invoices.filter((inv) => inv.status !== 'PAID').length;
  const overdueCount = invoices.filter((inv) => inv.status === 'OVERDUE').length;

  if (authLoading || !isAuthenticated) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (user?.role !== 'CUSTOMER') {
    return null;
  }

  return (
    <Layout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Customer Portal</h1>
          <p className="text-gray-500 mt-1">Welcome, {user.fullName}</p>
        </div>

        {/* Summary Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Outstanding Balance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(outstandingBalance)}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Unpaid Invoices</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{unpaidCount}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Overdue Invoices</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-red-600">{overdueCount}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Credit Balance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">
                {customer ? formatCurrency(customer.creditBalance.amount) : '$0.00'}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Invoices List */}
        <Card>
          <CardHeader>
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <CardTitle>My Invoices</CardTitle>
              <Select value={statusFilter || 'all'} onValueChange={(value) => {
                setStatusFilter(value === 'all' ? '' : value);
              }}>
                <SelectTrigger className="w-full sm:w-[180px]">
                  <SelectValue placeholder="All Statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="SENT">Sent</SelectItem>
                  <SelectItem value="PAID">Paid</SelectItem>
                  <SelectItem value="OVERDUE">Overdue</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent>
            {invoicesLoading ? (
              <div className="text-center py-8 text-gray-500">Loading invoices...</div>
            ) : invoices.length === 0 ? (
              <div className="text-center py-8 text-gray-500">No invoices found</div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Invoice Number</TableHead>
                      <TableHead className="hidden sm:table-cell">Issue Date</TableHead>
                      <TableHead className="hidden md:table-cell">Due Date</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Total</TableHead>
                      <TableHead className="text-right">Balance</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {invoices.map((invoice) => (
                      <TableRow key={invoice.id}>
                        <TableCell className="font-medium">{invoice.invoiceNumber}</TableCell>
                        <TableCell className="hidden sm:table-cell">
                          {formatDate(invoice.issueDate)}
                        </TableCell>
                        <TableCell className="hidden md:table-cell">
                          {formatDate(invoice.dueDate)}
                        </TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(invoice.status)}>
                            {invoice.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          {formatCurrency(invoice.totalAmount.amount)}
                        </TableCell>
                        <TableCell
                          className={`text-right font-medium ${
                            invoice.balanceDue.amount > 0 ? 'text-red-600' : 'text-green-600'
                          }`}
                        >
                          {formatCurrency(invoice.balanceDue.amount)}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex gap-2 justify-end">
                            <Link href={`/invoices/${invoice.id}`}>
                              <Button variant="ghost" size="sm" className="hidden sm:inline-flex">
                                View
                              </Button>
                            </Link>
                            {(invoice.status === 'SENT' || invoice.status === 'OVERDUE') &&
                              invoice.balanceDue.amount > 0 && (
                                <Button
                                  size="sm"
                                  onClick={() =>
                                    handlePayNow(invoice.id, invoice.balanceDue.amount)
                                  }
                                >
                                  Pay Now
                                </Button>
                              )}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Payment Dialog */}
        <Dialog open={paymentDialogOpen} onOpenChange={setPaymentDialogOpen}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Pay Invoice</DialogTitle>
              <DialogDescription>
                Pay your invoice. Balance due: {formatCurrency(selectedBalanceDue)}
              </DialogDescription>
            </DialogHeader>
            {selectedInvoiceId && (
              <PaymentForm
                invoiceId={selectedInvoiceId}
                balanceDue={selectedBalanceDue}
                onSuccess={handlePaymentRecorded}
                userRole="CUSTOMER"
              />
            )}
          </DialogContent>
        </Dialog>
      </div>
    </Layout>
  );
}

