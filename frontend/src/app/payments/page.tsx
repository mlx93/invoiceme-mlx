'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Layout } from '@/components/layout/Layout';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { usePayments } from '@/hooks/usePayments';
import { useAuth } from '@/contexts/AuthContext';
import { formatCurrency, formatDate } from '@/lib/utils';

export default function PaymentsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const [page, setPage] = useState(0);

  const invoiceId = searchParams.get('invoiceId') || undefined;
  const customerId = searchParams.get('customerId') || undefined;

  const { payments, loading, error, pagination } = usePayments({
    page,
    size: 20,
    invoiceId,
    customerId,
  });

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  if (authLoading || !isAuthenticated) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Payments</h1>

        {error && (
          <div className="p-4 bg-red-50 text-red-800 rounded">{error}</div>
        )}

        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">Loading...</div>
          </div>
        ) : (
          <>
            <div className="border rounded-lg">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Invoice Number</TableHead>
                    <TableHead>Customer</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Payment Method</TableHead>
                    <TableHead>Payment Date</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {payments.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-8 text-gray-500">
                        No payments found
                      </TableCell>
                    </TableRow>
                  ) : (
                    payments.map((payment) => (
                      <TableRow key={payment.id}>
                        <TableCell className="font-medium">{payment.invoiceNumber}</TableCell>
                        <TableCell>{payment.customerName}</TableCell>
                        <TableCell>{formatCurrency(payment.amount.amount)}</TableCell>
                        <TableCell>{payment.paymentMethod}</TableCell>
                        <TableCell>{formatDate(payment.paymentDate)}</TableCell>
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
                    ))
                  )}
                </TableBody>
              </Table>
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-between">
                <div className="text-sm text-gray-500">
                  Showing {pagination.page * pagination.size + 1} to{' '}
                  {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
                  {pagination.totalElements} payments
                </div>
                <div className="flex gap-2">
                  <button
                    className="px-4 py-2 border rounded disabled:opacity-50"
                    onClick={() => setPage(page - 1)}
                    disabled={pagination.first}
                  >
                    Previous
                  </button>
                  <button
                    className="px-4 py-2 border rounded disabled:opacity-50"
                    onClick={() => setPage(page + 1)}
                    disabled={pagination.last}
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  );
}

