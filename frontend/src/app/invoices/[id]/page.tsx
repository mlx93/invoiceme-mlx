'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useInvoice, useMarkInvoiceAsSent, useCancelInvoice } from '@/hooks/useInvoices';
import { useRecordPayment } from '@/hooks/usePayments';
import { useAuth } from '@/contexts/AuthContext';
import {
  canEditInvoice,
  canCancelInvoice,
  canRecordPayment,
  canIssueRefund,
} from '@/lib/rbac';
import { formatCurrency, formatDate, formatDateTime } from '@/lib/utils';
import { InvoiceStatus } from '@/types/common';
import { PaymentForm } from '@/components/payments/PaymentForm';

export default function InvoiceDetailPage() {
  const router = useRouter();
  const params = useParams();
  const invoiceId = params.id as string;
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { invoice, loading, error, refetch } = useInvoice(invoiceId);
  const { markAsSent, loading: markingSent } = useMarkInvoiceAsSent();
  const { cancelInvoice, loading: cancelling } = useCancelInvoice();
  const { recordPayment } = useRecordPayment();
  const [markSentDialogOpen, setMarkSentDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  const handleMarkAsSent = async () => {
    try {
      await markAsSent(invoiceId);
      setMarkSentDialogOpen(false);
      refetch?.();
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleCancel = async () => {
    try {
      await cancelInvoice(invoiceId);
      setCancelDialogOpen(false);
      router.push('/invoices');
    } catch (err) {
      // Error handled by hook
    }
  };

  const handlePaymentRecorded = () => {
    setPaymentDialogOpen(false);
    refetch?.();
  };

  const canEdit = invoice && user ? canEditInvoice(user.role, invoice.status) : false;
  const canCancel = invoice && user ? canCancelInvoice(user.role) : false;
  const canRecord = invoice && user
    ? canRecordPayment(user.role, invoice.customerId, user.customerId)
    : false;
  const canRefund = invoice && user ? canIssueRefund(user.role) : false;

  const getStatusBadgeVariant = (status: InvoiceStatus) => {
    switch (status) {
      case 'PAID':
        return 'default';
      case 'OVERDUE':
        return 'destructive';
      case 'CANCELLED':
        return 'secondary';
      default:
        return 'outline';
    }
  };

  if (authLoading || !isAuthenticated) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (error || !invoice) {
    return (
      <Layout>
        <div className="p-4 bg-red-50 text-red-800 rounded">{error || 'Invoice not found'}</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">{invoice.invoiceNumber}</h1>
            <p className="text-gray-500 mt-1">
              Customer: {invoice.customerName} | Issue Date: {formatDate(invoice.issueDate)} | Due
              Date: {formatDate(invoice.dueDate)}
            </p>
          </div>
          <div className="flex gap-2">
            {invoice.status === 'DRAFT' && canEdit && (
              <Link href={`/invoices/${invoice.id}/edit`}>
                <Button variant="outline">Edit</Button>
              </Link>
            )}
            {invoice.status === 'DRAFT' && canEdit && (
              <Dialog open={markSentDialogOpen} onOpenChange={setMarkSentDialogOpen}>
                <DialogTrigger asChild>
                  <Button>Send to Customer</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Mark Invoice as Sent</DialogTitle>
                    <DialogDescription>
                      This will change the invoice status from DRAFT to SENT. Are you sure?
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setMarkSentDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button onClick={handleMarkAsSent} disabled={markingSent}>
                      {markingSent ? 'Sending...' : 'Send to Customer'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            )}
            {(invoice.status === 'SENT' || invoice.status === 'OVERDUE') && canRecord && (
              <Dialog open={paymentDialogOpen} onOpenChange={setPaymentDialogOpen}>
                <DialogTrigger asChild>
                  <Button>Record Payment</Button>
                </DialogTrigger>
                <DialogContent className="max-w-md">
                  <DialogHeader>
                    <DialogTitle>Record Payment</DialogTitle>
                    <DialogDescription>
                      Record a payment for invoice {invoice.invoiceNumber}. Balance due:{' '}
                      {formatCurrency(invoice.balanceDue.amount)}
                    </DialogDescription>
                  </DialogHeader>
                  <PaymentForm
                    invoiceId={invoice.id}
                    balanceDue={invoice.balanceDue.amount}
                    onSuccess={handlePaymentRecorded}
                  />
                </DialogContent>
              </Dialog>
            )}
            {(invoice.status === 'DRAFT' || invoice.status === 'SENT') && canCancel && (
              <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="destructive">Cancel Invoice</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Cancel Invoice</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to cancel invoice {invoice.invoiceNumber}? This action
                      cannot be undone. Invoice must not have any payments applied.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button variant="destructive" onClick={handleCancel} disabled={cancelling}>
                      {cancelling ? 'Cancelling...' : 'Cancel Invoice'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            )}
            {invoice.status === 'PAID' && canRefund && (
              <Link href={`/invoices/${invoice.id}/refund`}>
                <Button variant="outline">Issue Refund</Button>
              </Link>
            )}
            <a href={invoice.pdfUrl} target="_blank" rel="noopener noreferrer">
              <Button variant="outline">Download PDF</Button>
            </a>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle>Invoice Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div>
                <p className="text-sm text-gray-500 mb-2">Status</p>
                <Badge variant={getStatusBadgeVariant(invoice.status)}>{invoice.status}</Badge>
              </div>

              {invoice.lineItems && invoice.lineItems.length > 0 && (
                <div>
                  <p className="text-sm text-gray-500 mb-2">Line Items</p>
                  <div className="border rounded-lg">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Description</TableHead>
                          <TableHead className="text-right">Quantity</TableHead>
                          <TableHead className="text-right">Unit Price</TableHead>
                          <TableHead className="text-right">Discount</TableHead>
                          <TableHead className="text-right">Tax Rate</TableHead>
                          <TableHead className="text-right">Line Total</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {invoice.lineItems.map((item) => (
                          <TableRow key={item.id}>
                            <TableCell className="font-medium">{item.description}</TableCell>
                            <TableCell className="text-right">{item.quantity}</TableCell>
                            <TableCell className="text-right">
                              {formatCurrency(item.unitPrice.amount)}
                            </TableCell>
                            <TableCell className="text-right">
                              {item.discountType !== 'NONE'
                                ? item.discountType === 'PERCENTAGE'
                                  ? `${item.discountValue.amount}%`
                                  : formatCurrency(item.discountValue.amount)
                                : '-'}
                            </TableCell>
                            <TableCell className="text-right">{item.taxRate}%</TableCell>
                            <TableCell className="text-right font-medium">
                              {formatCurrency(item.lineTotal.amount)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                </div>
              )}

              {invoice.notes && (
                <div>
                  <p className="text-sm text-gray-500 mb-2">Notes</p>
                  <p className="text-sm">{invoice.notes}</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Financial Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between">
                <span className="text-sm text-gray-500">Subtotal</span>
                <span className="font-medium">{formatCurrency(invoice.subtotal.amount)}</span>
              </div>
              {invoice.discountAmount.amount > 0 && (
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500">Discount</span>
                  <span className="font-medium text-green-600">
                    -{formatCurrency(invoice.discountAmount.amount)}
                  </span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-sm text-gray-500">Tax</span>
                <span className="font-medium">{formatCurrency(invoice.taxAmount.amount)}</span>
              </div>
              <div className="border-t pt-2 flex justify-between">
                <span className="font-medium">Total Amount</span>
                <span className="font-bold text-lg">{formatCurrency(invoice.totalAmount.amount)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-500">Amount Paid</span>
                <span className="font-medium text-green-600">
                  {formatCurrency(invoice.amountPaid.amount)}
                </span>
              </div>
              <div className="border-t pt-2 flex justify-between">
                <span className="font-medium">Balance Due</span>
                <span
                  className={`font-bold text-lg ${
                    invoice.balanceDue.amount > 0 ? 'text-red-600' : 'text-green-600'
                  }`}
                >
                  {formatCurrency(invoice.balanceDue.amount)}
                </span>
              </div>
              {invoice.sentDate && (
                <div className="pt-2 border-t">
                  <p className="text-xs text-gray-500">Sent Date</p>
                  <p className="text-sm">{formatDateTime(invoice.sentDate)}</p>
                </div>
              )}
              {invoice.paidDate && (
                <div>
                  <p className="text-xs text-gray-500">Paid Date</p>
                  <p className="text-sm">{formatDateTime(invoice.paidDate)}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {invoice.payments && invoice.payments.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>Payment History</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="border rounded-lg">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Date</TableHead>
                      <TableHead>Amount</TableHead>
                      <TableHead>Method</TableHead>
                      <TableHead>Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {invoice.payments.map((payment) => (
                      <TableRow key={payment.id}>
                        <TableCell>{formatDate(payment.paymentDate)}</TableCell>
                        <TableCell className="font-medium">
                          {formatCurrency(payment.amount.amount)}
                        </TableCell>
                        <TableCell>{payment.paymentMethod}</TableCell>
                        <TableCell>
                          <Badge
                            variant={
                              payment.status === 'COMPLETED'
                                ? 'default'
                                : payment.status === 'FAILED'
                                ? 'destructive'
                                : 'secondary'
                            }
                          >
                            {payment.status}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex gap-4">
          <Link href={`/customers/${invoice.customerId}`}>
            <Button variant="outline">View Customer</Button>
          </Link>
          <Link href={`/payments?invoiceId=${invoice.id}`}>
            <Button variant="outline">View All Payments</Button>
          </Link>
        </div>
      </div>
    </Layout>
  );
}

