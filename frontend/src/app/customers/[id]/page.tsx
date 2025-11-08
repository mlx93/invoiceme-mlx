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
import { useCustomer, useDeleteCustomer } from '@/hooks/useCustomers';
import { useAuth } from '@/contexts/AuthContext';
import { canEditCustomer, canDeleteCustomer } from '@/lib/rbac';
import { formatCurrency, formatDate } from '@/lib/utils';

export default function CustomerDetailPage() {
  const router = useRouter();
  const params = useParams();
  const customerId = params.id as string;
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { customer, loading, error } = useCustomer(customerId);
  const { deleteCustomer, loading: deleting } = useDeleteCustomer();
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  const handleDelete = async () => {
    try {
      await deleteCustomer(customerId);
      router.push('/customers');
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

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (error || !customer) {
    return (
      <Layout>
        <div className="p-4 bg-red-50 text-red-800 rounded">{error || 'Customer not found'}</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">{customer.companyName}</h1>
            <p className="text-gray-500 mt-1">{customer.email}</p>
          </div>
          <div className="flex gap-2">
            {user && canEditCustomer(user.role) && (
              <Link href={`/customers/${customer.id}/edit`}>
                <Button variant="outline">Edit</Button>
              </Link>
            )}
            {user && canDeleteCustomer(user.role) && (
              <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="destructive">Delete</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Delete Customer</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to delete {customer.companyName}? This action cannot be undone.
                      Customer must have zero balance and all invoices paid.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button variant="destructive" onClick={handleDelete} disabled={deleting}>
                      {deleting ? 'Deleting...' : 'Delete'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            )}
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Customer Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm text-gray-500">Company Name</p>
                <p className="font-medium">{customer.companyName}</p>
              </div>
              {customer.contactName && (
                <div>
                  <p className="text-sm text-gray-500">Contact Name</p>
                  <p className="font-medium">{customer.contactName}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="font-medium">{customer.email}</p>
              </div>
              {customer.phone && (
                <div>
                  <p className="text-sm text-gray-500">Phone</p>
                  <p className="font-medium">{customer.phone}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-gray-500">Type</p>
                <Badge variant="outline">{customer.customerType}</Badge>
              </div>
              <div>
                <p className="text-sm text-gray-500">Status</p>
                <Badge
                  variant={
                    customer.status === 'ACTIVE'
                      ? 'default'
                      : customer.status === 'SUSPENDED'
                      ? 'destructive'
                      : 'secondary'
                  }
                >
                  {customer.status}
                </Badge>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Financial Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm text-gray-500">Credit Balance</p>
                <p className="text-2xl font-bold">{formatCurrency(customer.creditBalance.amount)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Outstanding Balance</p>
                <p className="text-2xl font-bold text-red-600">
                  {formatCurrency(customer.outstandingBalance.amount)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Total Invoices</p>
                <p className="font-medium">{customer.totalInvoices}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Unpaid Invoices</p>
                <p className="font-medium">{customer.unpaidInvoices}</p>
              </div>
            </CardContent>
          </Card>

          {customer.address && (
            <Card>
              <CardHeader>
                <CardTitle>Address</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="font-medium">
                  {[
                    customer.address.street,
                    customer.address.city,
                    customer.address.state,
                    customer.address.zipCode,
                    customer.address.country,
                  ]
                    .filter(Boolean)
                    .join(', ')}
                </p>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="flex gap-4">
          <Link href={`/invoices?customerId=${customer.id}`}>
            <Button variant="outline">View Invoices</Button>
          </Link>
          <Link href={`/payments?customerId=${customer.id}`}>
            <Button variant="outline">View Payments</Button>
          </Link>
        </div>
      </div>
    </Layout>
  );
}

