'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
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
import { Alert, AlertDescription } from '@/components/ui/alert';
import { usePendingUsers, useApproveUser, useRejectUser } from '@/hooks/useUsers';
import { useAuth } from '@/contexts/AuthContext';
import { canApproveUsers } from '@/lib/rbac';
import { formatDate } from '@/lib/utils';
import { UserRole } from '@/types/user';

export default function PendingUsersPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { users, loading, error, refetch } = usePendingUsers();
  const { approveUser, loading: approving, error: approveError } = useApproveUser();
  const { rejectUser, loading: rejecting, error: rejectError } = useRejectUser();
  const [actionDialogOpen, setActionDialogOpen] = useState<string | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [actionType, setActionType] = useState<'approve' | 'reject' | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canApproveUsers(user.role)) {
      router.push('/dashboard');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const handleApprove = async (id: string) => {
    try {
      await approveUser(id);
      setActionDialogOpen(null);
      refetch();
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleReject = async (id: string) => {
    try {
      await rejectUser(id);
      setActionDialogOpen(null);
      refetch();
    } catch (err) {
      // Error handled by hook
    }
  };

  const getRoleBadgeVariant = (role: UserRole) => {
    switch (role) {
      case 'SYSADMIN':
        return 'destructive';
      case 'ACCOUNTANT':
        return 'default';
      case 'SALES':
        return 'secondary';
      case 'CUSTOMER':
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
          <h1 className="text-3xl font-bold">Pending User Approvals</h1>
        </div>

        {(approveError || rejectError) && (
          <Alert variant="destructive">
            <AlertDescription>{approveError || rejectError}</AlertDescription>
          </Alert>
        )}

        {error && (
          <div className="p-4 bg-red-50 text-red-800 rounded">{error}</div>
        )}

        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">Loading...</div>
          </div>
        ) : users.length === 0 ? (
          <div className="p-8 text-center text-gray-500 border rounded-lg">
            No pending user registrations
          </div>
        ) : (
          <>
            <div className="border rounded-lg overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Registered</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.map((pendingUser) => (
                    <TableRow key={pendingUser.id} className="hover:bg-gray-100 transition-colors">
                      <TableCell className="font-medium">{pendingUser.fullName}</TableCell>
                      <TableCell>{pendingUser.email}</TableCell>
                      <TableCell>
                        <Badge variant={getRoleBadgeVariant(pendingUser.role)}>
                          {pendingUser.role}
                        </Badge>
                      </TableCell>
                      <TableCell>{formatDate(pendingUser.createdAt)}</TableCell>
                      <TableCell>
                        <Badge variant="secondary">{pendingUser.status}</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex gap-2 justify-end">
                          <Dialog
                            open={actionDialogOpen === `approve-${pendingUser.id}`}
                            onOpenChange={(open) => {
                              setActionDialogOpen(open ? `approve-${pendingUser.id}` : null);
                              setSelectedUserId(pendingUser.id);
                              setActionType(open ? 'approve' : null);
                            }}
                          >
                            <DialogTrigger asChild>
                              <Button variant="default" size="sm">
                                Approve
                              </Button>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Approve User</DialogTitle>
                                <DialogDescription>
                                  Are you sure you want to approve {pendingUser.fullName} (
                                  {pendingUser.email}) as {pendingUser.role}? They will be able to log in immediately.
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
                                  onClick={() => handleApprove(pendingUser.id)}
                                  disabled={approving}
                                >
                                  {approving ? 'Approving...' : 'Approve'}
                                </Button>
                              </DialogFooter>
                            </DialogContent>
                          </Dialog>

                          <Dialog
                            open={actionDialogOpen === `reject-${pendingUser.id}`}
                            onOpenChange={(open) => {
                              setActionDialogOpen(open ? `reject-${pendingUser.id}` : null);
                              setSelectedUserId(pendingUser.id);
                              setActionType(open ? 'reject' : null);
                            }}
                          >
                            <DialogTrigger asChild>
                              <Button variant="destructive" size="sm">
                                Reject
                              </Button>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Reject User</DialogTitle>
                                <DialogDescription>
                                  Are you sure you want to reject {pendingUser.fullName} (
                                  {pendingUser.email})? They will not be able to log in. This action cannot be undone.
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
                                  onClick={() => handleReject(pendingUser.id)}
                                  disabled={rejecting}
                                >
                                  {rejecting ? 'Rejecting...' : 'Reject'}
                                </Button>
                              </DialogFooter>
                            </DialogContent>
                          </Dialog>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </>
        )}
      </div>
    </Layout>
  );
}

