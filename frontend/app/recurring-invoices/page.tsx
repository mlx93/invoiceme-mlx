'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { useRecurringInvoices, usePauseTemplate, useResumeTemplate, useCompleteTemplate } from '@/hooks/useRecurringInvoices';
import { useAuth } from '@/contexts/AuthContext';
import { canManageRecurringInvoices } from '@/lib/rbac';
import { formatDate } from '@/lib/utils';
import { RecurringStatus } from '@/types/common';

export default function RecurringInvoicesPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const [statusFilter, setStatusFilter] = useState<string>('');
  const { templates, loading, error, pagination, refetch } = useRecurringInvoices({
    size: 20,
    status: statusFilter || undefined,
  });
  const { pauseTemplate, loading: pausing } = usePauseTemplate();
  const { resumeTemplate, loading: resuming } = useResumeTemplate();
  const { completeTemplate, loading: completing } = useCompleteTemplate();
  const [actionDialogOpen, setActionDialogOpen] = useState<string | null>(null);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canManageRecurringInvoices(user.role)) {
      router.push('/dashboard');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const handlePause = async (id: string) => {
    try {
      await pauseTemplate(id);
      setActionDialogOpen(null);
      refetch();
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleResume = async (id: string) => {
    try {
      await resumeTemplate(id);
      setActionDialogOpen(null);
      refetch();
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleComplete = async (id: string) => {
    try {
      await completeTemplate(id);
      setActionDialogOpen(null);
      refetch();
    } catch (err) {
      // Error handled by hook
    }
  };

  const getStatusBadgeVariant = (status: RecurringStatus) => {
    switch (status) {
      case 'ACTIVE':
        return 'default';
      case 'PAUSED':
        return 'secondary';
      case 'COMPLETED':
        return 'outline';
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

  return (
    <Layout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Recurring Invoices</h1>
          {user && canManageRecurringInvoices(user.role) && (
            <Link href="/recurring-invoices/new">
              <Button>Create Template</Button>
            </Link>
          )}
        </div>

        {/* Filters */}
        <div className="flex gap-4">
          <Select value={statusFilter || 'all'} onValueChange={(value) => {
            setStatusFilter(value === 'all' ? '' : value);
          }}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="All Statuses" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              <SelectItem value="ACTIVE">Active</SelectItem>
              <SelectItem value="PAUSED">Paused</SelectItem>
              <SelectItem value="COMPLETED">Completed</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {error && (
          <div className="p-4 bg-red-50 text-red-800 rounded">{error}</div>
        )}

        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">Loading...</div>
          </div>
        ) : (
          <>
            <div className="border rounded-lg overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Template Name</TableHead>
                    <TableHead>Customer</TableHead>
                    <TableHead>Frequency</TableHead>
                    <TableHead>Next Invoice Date</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Auto-Send</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {templates.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                        No recurring invoice templates found
                      </TableCell>
                    </TableRow>
                  ) : (
                    templates.map((template) => (
                      <TableRow key={template.id}>
                        <TableCell className="font-medium">{template.templateName}</TableCell>
                        <TableCell>{template.customerName}</TableCell>
                        <TableCell>{template.frequency}</TableCell>
                        <TableCell>
                          {template.nextInvoiceDate ? formatDate(template.nextInvoiceDate) : '-'}
                        </TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(template.status)}>
                            {template.status}
                          </Badge>
                        </TableCell>
                        <TableCell>{template.autoSend ? 'Yes' : 'No'}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex gap-2 justify-end">
                            <Link href={`/recurring-invoices/${template.id}`}>
                              <Button variant="ghost" size="sm">
                                View
                              </Button>
                            </Link>
                            {template.status === 'ACTIVE' && (
                              <Dialog
                                open={actionDialogOpen === `pause-${template.id}`}
                                onOpenChange={(open) => {
                                  setActionDialogOpen(open ? `pause-${template.id}` : null);
                                  setSelectedTemplateId(template.id);
                                }}
                              >
                                <DialogTrigger asChild>
                                  <Button variant="outline" size="sm">
                                    Pause
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Pause Template</DialogTitle>
                                    <DialogDescription>
                                      Are you sure you want to pause template "{template.templateName}"? This will stop auto-generation of invoices.
                                    </DialogDescription>
                                  </DialogHeader>
                                  <DialogFooter>
                                    <Button
                                      variant="outline"
                                      onClick={() => setActionDialogOpen(null)}
                                    >
                                      Cancel
                                    </Button>
                                    <Button
                                      onClick={() => handlePause(template.id)}
                                      disabled={pausing}
                                    >
                                      {pausing ? 'Pausing...' : 'Pause'}
                                    </Button>
                                  </DialogFooter>
                                </DialogContent>
                              </Dialog>
                            )}
                            {template.status === 'PAUSED' && (
                              <Dialog
                                open={actionDialogOpen === `resume-${template.id}`}
                                onOpenChange={(open) => {
                                  setActionDialogOpen(open ? `resume-${template.id}` : null);
                                  setSelectedTemplateId(template.id);
                                }}
                              >
                                <DialogTrigger asChild>
                                  <Button variant="outline" size="sm">
                                    Resume
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Resume Template</DialogTitle>
                                    <DialogDescription>
                                      Are you sure you want to resume template "{template.templateName}"? This will restart auto-generation of invoices.
                                    </DialogDescription>
                                  </DialogHeader>
                                  <DialogFooter>
                                    <Button
                                      variant="outline"
                                      onClick={() => setActionDialogOpen(null)}
                                    >
                                      Cancel
                                    </Button>
                                    <Button
                                      onClick={() => handleResume(template.id)}
                                      disabled={resuming}
                                    >
                                      {resuming ? 'Resuming...' : 'Resume'}
                                    </Button>
                                  </DialogFooter>
                                </DialogContent>
                              </Dialog>
                            )}
                            {template.status !== 'COMPLETED' && (
                              <Dialog
                                open={actionDialogOpen === `complete-${template.id}`}
                                onOpenChange={(open) => {
                                  setActionDialogOpen(open ? `complete-${template.id}` : null);
                                  setSelectedTemplateId(template.id);
                                }}
                              >
                                <DialogTrigger asChild>
                                  <Button variant="destructive" size="sm">
                                    Complete
                                  </Button>
                                </DialogTrigger>
                                <DialogContent>
                                  <DialogHeader>
                                    <DialogTitle>Complete Template</DialogTitle>
                                    <DialogDescription>
                                      Are you sure you want to complete template "{template.templateName}"? This will stop all future invoice generation and cannot be undone.
                                    </DialogDescription>
                                  </DialogHeader>
                                  <DialogFooter>
                                    <Button
                                      variant="outline"
                                      onClick={() => setActionDialogOpen(null)}
                                    >
                                      Cancel
                                    </Button>
                                    <Button
                                      variant="destructive"
                                      onClick={() => handleComplete(template.id)}
                                      disabled={completing}
                                    >
                                      {completing ? 'Completing...' : 'Complete'}
                                    </Button>
                                  </DialogFooter>
                                </DialogContent>
                              </Dialog>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          </>
        )}
      </div>
    </Layout>
  );
}

