'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
import { useRecurringInvoice, usePauseTemplate, useResumeTemplate, useCompleteTemplate } from '@/hooks/useRecurringInvoices';
import { useAuth } from '@/contexts/AuthContext';
import { canManageRecurringInvoices } from '@/lib/rbac';
import { formatDate } from '@/lib/utils';
import { formatCurrency } from '@/lib/utils';
import { RecurringStatus } from '@/types/common';

export default function RecurringInvoiceDetailPage() {
  const router = useRouter();
  const params = useParams();
  const templateId = params.id as string;
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { template, loading, error } = useRecurringInvoice(templateId);
  const { pauseTemplate, loading: pausing } = usePauseTemplate();
  const { resumeTemplate, loading: resuming } = useResumeTemplate();
  const { completeTemplate, loading: completing } = useCompleteTemplate();
  const [pauseDialogOpen, setPauseDialogOpen] = useState(false);
  const [resumeDialogOpen, setResumeDialogOpen] = useState(false);
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canManageRecurringInvoices(user.role)) {
      router.push('/dashboard');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const handlePause = async () => {
    try {
      await pauseTemplate(templateId);
      setPauseDialogOpen(false);
      router.push('/recurring-invoices');
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleResume = async () => {
    try {
      await resumeTemplate(templateId);
      setResumeDialogOpen(false);
      router.push('/recurring-invoices');
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleComplete = async () => {
    try {
      await completeTemplate(templateId);
      setCompleteDialogOpen(false);
      router.push('/recurring-invoices');
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

  if (error || !template) {
    return (
      <Layout>
        <div className="p-4 bg-red-50 text-red-800 rounded">{error || 'Template not found'}</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">{template.templateName}</h1>
            <p className="text-gray-500 mt-1">
              Customer: {template.customerName} | Frequency: {template.frequency} | Start Date:{' '}
              {formatDate(template.startDate)}
            </p>
          </div>
          <div className="flex gap-2">
            {template.status === 'ACTIVE' && (
              <Dialog open={pauseDialogOpen} onOpenChange={setPauseDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="outline">Pause</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Pause Template</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to pause this template? This will stop auto-generation
                      of invoices.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setPauseDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button onClick={handlePause} disabled={pausing}>
                      {pausing ? 'Pausing...' : 'Pause'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            )}
            {template.status === 'PAUSED' && (
              <Dialog open={resumeDialogOpen} onOpenChange={setResumeDialogOpen}>
                <DialogTrigger asChild>
                  <Button>Resume</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Resume Template</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to resume this template? This will restart
                      auto-generation of invoices.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setResumeDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button onClick={handleResume} disabled={resuming}>
                      {resuming ? 'Resuming...' : 'Resume'}
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            )}
            {template.status !== 'COMPLETED' && (
              <Dialog open={completeDialogOpen} onOpenChange={setCompleteDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="destructive">Complete</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Complete Template</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to complete this template? This will stop all future
                      invoice generation and cannot be undone.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button variant="outline" onClick={() => setCompleteDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button variant="destructive" onClick={handleComplete} disabled={completing}>
                      {completing ? 'Completing...' : 'Complete'}
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
              <CardTitle>Template Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm text-gray-500">Template Name</p>
                <p className="font-medium">{template.templateName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Customer</p>
                <p className="font-medium">{template.customerName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Frequency</p>
                <Badge variant="outline">{template.frequency}</Badge>
              </div>
              <div>
                <p className="text-sm text-gray-500">Status</p>
                <Badge
                  variant={
                    template.status === 'ACTIVE'
                      ? 'default'
                      : template.status === 'PAUSED'
                      ? 'secondary'
                      : 'outline'
                  }
                >
                  {template.status}
                </Badge>
              </div>
              <div>
                <p className="text-sm text-gray-500">Start Date</p>
                <p className="font-medium">{formatDate(template.startDate)}</p>
              </div>
              {template.endDate && (
                <div>
                  <p className="text-sm text-gray-500">End Date</p>
                  <p className="font-medium">{formatDate(template.endDate)}</p>
                </div>
              )}
              {template.nextInvoiceDate && (
                <div>
                  <p className="text-sm text-gray-500">Next Invoice Date</p>
                  <p className="font-medium">{formatDate(template.nextInvoiceDate)}</p>
                </div>
              )}
              <div>
                <p className="text-sm text-gray-500">Payment Terms</p>
                <p className="font-medium">{template.paymentTerms}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Auto-Send</p>
                <p className="font-medium">{template.autoSend ? 'Yes' : 'No'}</p>
              </div>
            </CardContent>
          </Card>

          {template.lineItems && template.lineItems.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Line Items</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="border rounded-lg">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Description</TableHead>
                        <TableHead className="text-right">Quantity</TableHead>
                        <TableHead className="text-right">Unit Price</TableHead>
                        <TableHead className="text-right">Tax Rate</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {template.lineItems.map((item, index) => (
                        <TableRow key={index}>
                          <TableCell className="font-medium">{item.description}</TableCell>
                          <TableCell className="text-right">{item.quantity}</TableCell>
                          <TableCell className="text-right">
                            ${item.unitPrice.toFixed(2)}
                          </TableCell>
                          <TableCell className="text-right">{item.taxRate}%</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="flex gap-4">
          <Link href={`/customers/${template.customerId}`}>
            <Button variant="outline">View Customer</Button>
          </Link>
          <Link href="/recurring-invoices">
            <Button variant="outline">Back to Templates</Button>
          </Link>
        </div>
      </div>
    </Layout>
  );
}

