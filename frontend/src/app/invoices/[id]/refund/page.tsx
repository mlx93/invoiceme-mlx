'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
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
import { useInvoice } from '@/hooks/useInvoices';
import { useIssueRefund, useRefunds } from '@/hooks/useRefunds';
import { useAuth } from '@/contexts/AuthContext';
import { canIssueRefund } from '@/lib/rbac';
import { formatCurrency, formatDate, formatDateTime } from '@/lib/utils';

const refundSchema = z.object({
  amount: z.number().min(0.01, 'Amount must be greater than 0'),
  reason: z.string().min(1, 'Reason is required').max(500, 'Reason must be 500 characters or less'),
  applyAsCredit: z.boolean().default(false),
});

type RefundFormData = z.infer<typeof refundSchema>;

export default function RefundPage() {
  const router = useRouter();
  const params = useParams();
  const invoiceId = params.id as string;
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { invoice, loading: invoiceLoading, error: invoiceError } = useInvoice(invoiceId);
  const { issueRefund, loading: issuing, error: refundError } = useIssueRefund();
  const { refunds, loading: refundsLoading, refetch: refetchRefunds } = useRefunds({
    invoiceId,
  });

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<RefundFormData>({
    resolver: zodResolver(refundSchema),
    defaultValues: {
      amount: 0,
      reason: '',
      applyAsCredit: false,
    },
  });

  const amountPaid = invoice?.amountPaid.amount || 0;
  const refundAmount = watch('amount');

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canIssueRefund(user.role)) {
      router.push(`/invoices/${invoiceId}`);
    }
  }, [authLoading, isAuthenticated, user, router, invoiceId]);

  useEffect(() => {
    if (invoice && amountPaid > 0) {
      setValue('amount', amountPaid);
    }
  }, [invoice, amountPaid, setValue]);

  const onSubmit = async (data: RefundFormData) => {
    if (!invoice) return;

    try {
      await issueRefund({
        invoiceId: invoice.id,
        amount: data.amount,
        reason: data.reason,
        applyAsCredit: data.applyAsCredit,
      });
      router.push(`/invoices/${invoiceId}`);
    } catch (err) {
      // Error handled by hook
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

  if (invoiceLoading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (invoiceError || !invoice) {
    return (
      <Layout>
        <div className="p-4 bg-red-50 text-red-800 rounded">
          {invoiceError || 'Invoice not found'}
        </div>
      </Layout>
    );
  }

  if (invoice.status !== 'PAID') {
    return (
      <Layout>
        <div className="p-4 bg-yellow-50 text-yellow-800 rounded">
          Refunds can only be issued on paid invoices. This invoice is currently {invoice.status}.
        </div>
        <div className="mt-4">
          <Link href={`/invoices/${invoiceId}`}>
            <Button variant="outline">Back to Invoice</Button>
          </Link>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Issue Refund</h1>
            <p className="text-gray-500 mt-1">
              Invoice: {invoice.invoiceNumber} | Amount Paid: {formatCurrency(amountPaid)}
            </p>
          </div>
          <Link href={`/invoices/${invoiceId}`}>
            <Button variant="outline">Back to Invoice</Button>
          </Link>
        </div>

        {refundError && (
          <Alert variant="destructive">
            <AlertDescription>{refundError}</AlertDescription>
          </Alert>
        )}

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Refund Information</CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                  <Label htmlFor="amount">Refund Amount *</Label>
                  <Input
                    id="amount"
                    type="number"
                    step="0.01"
                    min="0.01"
                    max={amountPaid}
                    {...register('amount', { valueAsNumber: true })}
                    className="mt-1"
                  />
                  {errors.amount && (
                    <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
                  )}
                  <p className="mt-1 text-xs text-gray-500">
                    Maximum refund: {formatCurrency(amountPaid)}
                  </p>
                </div>

                <div>
                  <Label htmlFor="reason">Reason *</Label>
                  <Textarea
                    id="reason"
                    placeholder="Enter reason for refund..."
                    {...register('reason')}
                    className="mt-1"
                    rows={4}
                  />
                  {errors.reason && (
                    <p className="mt-1 text-sm text-red-600">{errors.reason.message}</p>
                  )}
                </div>

                <div className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    id="applyAsCredit"
                    {...register('applyAsCredit')}
                    className="rounded"
                  />
                  <Label htmlFor="applyAsCredit" className="cursor-pointer">
                    Apply refund as customer credit (instead of external refund)
                  </Label>
                </div>

                {refundAmount > amountPaid && (
                  <Alert variant="destructive">
                    <AlertDescription>
                      Refund amount cannot exceed amount paid ({formatCurrency(amountPaid)})
                    </AlertDescription>
                  </Alert>
                )}

                {refundAmount < amountPaid && (
                  <Alert>
                    <AlertDescription>
                      Partial refund: {formatCurrency(amountPaid - refundAmount)} will remain on
                      invoice. Invoice status will change from PAID to SENT.
                    </AlertDescription>
                  </Alert>
                )}

                <div className="flex gap-4 pt-4">
                  <Button type="submit" disabled={issuing || refundAmount > amountPaid}>
                    {issuing ? 'Issuing Refund...' : 'Issue Refund'}
                  </Button>
                  <Button type="button" variant="outline" onClick={() => router.back()}>
                    Cancel
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Refund History</CardTitle>
            </CardHeader>
            <CardContent>
              {refundsLoading ? (
                <div className="text-center py-8 text-gray-500">Loading refunds...</div>
              ) : refunds.length === 0 ? (
                <div className="text-center py-8 text-gray-500">No refunds issued yet</div>
              ) : (
                <div className="border rounded-lg overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Date</TableHead>
                        <TableHead>Amount</TableHead>
                        <TableHead>Reason</TableHead>
                        <TableHead>Type</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {refunds.map((refund) => (
                        <TableRow key={refund.id}>
                          <TableCell>{formatDateTime(refund.createdAt)}</TableCell>
                          <TableCell className="font-medium">
                            {formatCurrency(refund.amount.amount)}
                          </TableCell>
                          <TableCell className="max-w-xs truncate">{refund.reason}</TableCell>
                          <TableCell>
                            <Badge variant={refund.applyAsCredit ? 'default' : 'outline'}>
                              {refund.applyAsCredit ? 'Credit' : 'External'}
                            </Badge>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </Layout>
  );
}

