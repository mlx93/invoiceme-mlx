'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useCreateInvoice, useMarkInvoiceAsSent } from '@/hooks/useInvoices';
import { useCustomers } from '@/hooks/useCustomers';
import { useAuth } from '@/contexts/AuthContext';
import { canCreateInvoice } from '@/lib/rbac';
import { formatCurrency } from '@/lib/utils';
import { PaymentTerms, DiscountType } from '@/types/common';

const lineItemSchema = z.object({
  description: z.string().min(1, 'Description is required'),
  quantity: z.number().min(1, 'Quantity must be at least 1'),
  unitPrice: z.number().min(0, 'Unit price must be 0 or greater'),
  discountType: z.enum(['NONE', 'PERCENTAGE', 'FIXED']),
  discountValue: z.number().min(0).optional(),
  taxRate: z.number().min(0).max(100, 'Tax rate must be between 0 and 100'),
});

const invoiceSchema = z.object({
  customerId: z.string().uuid('Please select a customer'),
  issueDate: z.string().min(1, 'Issue date is required'),
  dueDate: z.string().optional(),
  paymentTerms: z.enum(['NET_30', 'DUE_ON_RECEIPT', 'CUSTOM']),
  lineItems: z.array(lineItemSchema).min(1, 'At least one line item is required'),
  notes: z.string().optional(),
});

type InvoiceFormData = z.infer<typeof invoiceSchema>;

function calculateLineTotal(item: z.infer<typeof lineItemSchema>): number {
  let baseAmount = item.quantity * item.unitPrice;
  let discountAmount = 0;

  if (item.discountType === 'PERCENTAGE' && item.discountValue) {
    discountAmount = baseAmount * (item.discountValue / 100);
  } else if (item.discountType === 'FIXED' && item.discountValue) {
    discountAmount = item.discountValue;
  }

  const taxableAmount = baseAmount - discountAmount;
  const taxAmount = taxableAmount * (item.taxRate / 100);
  return taxableAmount + taxAmount;
}

export default function CreateInvoicePage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { createInvoice, loading, error } = useCreateInvoice();
  const { markAsSent, loading: sendingInvoice } = useMarkInvoiceAsSent();
  const { customers } = useCustomers({ size: 1000 }); // Get all customers for dropdown
  const [successDialogOpen, setSuccessDialogOpen] = useState(false);
  const [createdInvoiceId, setCreatedInvoiceId] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    control,
    watch,
    setValue,
    formState: { errors },
  } = useForm<InvoiceFormData>({
    resolver: zodResolver(invoiceSchema),
    defaultValues: {
      paymentTerms: 'NET_30',
      issueDate: new Date().toISOString().split('T')[0],
      lineItems: [
        {
          description: '',
          quantity: 1,
          unitPrice: 0,
          discountType: 'NONE',
          discountValue: 0,
          taxRate: 0,
        },
      ],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  const paymentTerms = watch('paymentTerms');
  const issueDate = watch('issueDate');
  const lineItems = watch('lineItems');

  // Calculate due date based on payment terms
  useEffect(() => {
    if (issueDate && paymentTerms === 'NET_30') {
      const date = new Date(issueDate);
      date.setDate(date.getDate() + 30);
      setValue('dueDate', date.toISOString().split('T')[0]);
    } else if (issueDate && paymentTerms === 'DUE_ON_RECEIPT') {
      setValue('dueDate', issueDate);
    }
  }, [issueDate, paymentTerms, setValue]);

  // Calculate totals
  const subtotal = lineItems.reduce((sum, item) => {
    return sum + item.quantity * item.unitPrice;
  }, 0);

  const totalDiscount = lineItems.reduce((sum, item) => {
    const baseAmount = item.quantity * item.unitPrice;
    if (item.discountType === 'PERCENTAGE' && item.discountValue) {
      return sum + baseAmount * (item.discountValue / 100);
    } else if (item.discountType === 'FIXED' && item.discountValue) {
      return sum + item.discountValue;
    }
    return sum;
  }, 0);

  const totalTax = lineItems.reduce((sum, item) => {
    const baseAmount = item.quantity * item.unitPrice;
    let discountAmount = 0;
    if (item.discountType === 'PERCENTAGE' && item.discountValue) {
      discountAmount = baseAmount * (item.discountValue / 100);
    } else if (item.discountType === 'FIXED' && item.discountValue) {
      discountAmount = item.discountValue;
    }
    const taxableAmount = baseAmount - discountAmount;
    return sum + taxableAmount * (item.taxRate / 100);
  }, 0);

  const totalAmount = subtotal - totalDiscount + totalTax;

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canCreateInvoice(user.role)) {
      router.push('/invoices');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const onSubmit = async (data: InvoiceFormData) => {
    try {
      const invoice = await createInvoice({
        customerId: data.customerId,
        issueDate: data.issueDate,
        dueDate: data.dueDate,
        paymentTerms: data.paymentTerms,
        lineItems: data.lineItems.map((item) => ({
          description: item.description,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          discountType: item.discountType,
          discountValue: item.discountValue || 0,
          taxRate: item.taxRate,
        })),
        notes: data.notes,
      });
      setCreatedInvoiceId(invoice.id);
      setSuccessDialogOpen(true);
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleSendInvoice = async () => {
    if (!createdInvoiceId) return;
    try {
      const sentInvoice = await markAsSent(createdInvoiceId);
      setSuccessDialogOpen(false);
      
      // Check if invoice was paid with credit (balance is zero after sending)
      if (sentInvoice.balanceDue?.amount === 0 && sentInvoice.amountPaid?.amount === 0) {
        alert('✅ Invoice sent and paid in full with account credit!');
      }
      
      router.push(`/invoices/${createdInvoiceId}`);
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleViewInvoice = () => {
    if (!createdInvoiceId) return;
    setSuccessDialogOpen(false);
    router.push(`/invoices/${createdInvoiceId}`);
  };

  const handleCreateAnother = () => {
    setSuccessDialogOpen(false);
    setCreatedInvoiceId(null);
    router.refresh();
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
      <div className="max-w-6xl mx-auto space-y-6">
        <h1 className="text-3xl font-bold">Create Invoice</h1>

        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Invoice Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="customerId">Customer *</Label>
                  <Select
                    onValueChange={(value) => setValue('customerId', value)}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue placeholder="Select a customer" />
                    </SelectTrigger>
                    <SelectContent>
                      {customers.map((customer) => (
                        <SelectItem key={customer.id} value={customer.id}>
                          {customer.companyName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.customerId && (
                    <p className="mt-1 text-sm text-red-600">{errors.customerId.message}</p>
                  )}
                </div>

                <div>
                  <Label htmlFor="issueDate">Issue Date *</Label>
                  <Input
                    id="issueDate"
                    type="date"
                    {...register('issueDate')}
                    className="mt-1"
                  />
                  {errors.issueDate && (
                    <p className="mt-1 text-sm text-red-600">{errors.issueDate.message}</p>
                  )}
                </div>

                <div>
                  <Label htmlFor="paymentTerms">Payment Terms *</Label>
                  <Select
                    value={paymentTerms}
                    onValueChange={(value) => setValue('paymentTerms', value as PaymentTerms)}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="NET_30">Net 30</SelectItem>
                      <SelectItem value="DUE_ON_RECEIPT">Due on Receipt</SelectItem>
                      <SelectItem value="CUSTOM">Custom</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {paymentTerms === 'CUSTOM' && (
                  <div>
                    <Label htmlFor="dueDate">Due Date *</Label>
                    <Input
                      id="dueDate"
                      type="date"
                      {...register('dueDate')}
                      className="mt-1"
                    />
                    {errors.dueDate && (
                      <p className="mt-1 text-sm text-red-600">{errors.dueDate.message}</p>
                    )}
                  </div>
                )}

                <div>
                  <Label htmlFor="notes">Notes</Label>
                  <Textarea
                    id="notes"
                    placeholder="Optional notes"
                    {...register('notes')}
                    className="mt-1"
                  />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Invoice Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500">Subtotal</span>
                  <span className="font-medium">{formatCurrency(subtotal)}</span>
                </div>
                {totalDiscount > 0 && (
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-500">Discount</span>
                    <span className="font-medium text-green-600">
                      -{formatCurrency(totalDiscount)}
                    </span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-sm text-gray-500">Tax</span>
                  <span className="font-medium">{formatCurrency(totalTax)}</span>
                </div>
                <div className="border-t pt-2 flex justify-between">
                  <span className="font-medium">Total Amount</span>
                  <span className="font-bold text-lg">{formatCurrency(totalAmount)}</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Line Items</CardTitle>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() =>
                    append({
                      description: '',
                      quantity: 1,
                      unitPrice: 0,
                      discountType: 'NONE',
                      discountValue: 0,
                      taxRate: 0,
                    })
                  }
                >
                  Add Line Item
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {errors.lineItems && (
                <Alert variant="destructive" className="mb-4">
                  <AlertDescription>{errors.lineItems.message}</AlertDescription>
                </Alert>
              )}

              <div className="border rounded-lg overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Description</TableHead>
                      <TableHead className="w-24">Quantity</TableHead>
                      <TableHead className="w-32">Unit Price</TableHead>
                      <TableHead className="w-32">Discount Type</TableHead>
                      <TableHead className="w-24">Discount</TableHead>
                      <TableHead className="w-24">Tax Rate %</TableHead>
                      <TableHead className="w-32 text-right">Line Total</TableHead>
                      <TableHead className="w-16"></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {fields.map((field, index) => {
                      const item = lineItems[index];
                      const lineTotal = item ? calculateLineTotal(item) : 0;
                      return (
                        <TableRow key={field.id}>
                          <TableCell>
                            <Input
                              {...register(`lineItems.${index}.description`)}
                              placeholder="Item description"
                              className="w-full"
                            />
                            {errors.lineItems?.[index]?.description && (
                              <p className="text-xs text-red-600 mt-1">
                                {errors.lineItems[index]?.description?.message}
                              </p>
                            )}
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              min="1"
                              {...register(`lineItems.${index}.quantity`, {
                                valueAsNumber: true,
                              })}
                              className="w-full"
                            />
                            {errors.lineItems?.[index]?.quantity && (
                              <p className="text-xs text-red-600 mt-1">
                                {errors.lineItems[index]?.quantity?.message}
                              </p>
                            )}
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              step="0.01"
                              min="0"
                              {...register(`lineItems.${index}.unitPrice`, {
                                valueAsNumber: true,
                              })}
                              className="w-full"
                            />
                            {errors.lineItems?.[index]?.unitPrice && (
                              <p className="text-xs text-red-600 mt-1">
                                {errors.lineItems[index]?.unitPrice?.message}
                              </p>
                            )}
                          </TableCell>
                          <TableCell>
                            <Select
                              value={item?.discountType || 'NONE'}
                              onValueChange={(value) =>
                                setValue(`lineItems.${index}.discountType`, value as DiscountType)
                              }
                            >
                              <SelectTrigger className="w-full">
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="NONE">None</SelectItem>
                                <SelectItem value="PERCENTAGE">Percentage</SelectItem>
                                <SelectItem value="FIXED">Fixed</SelectItem>
                              </SelectContent>
                            </Select>
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              step="0.01"
                              min="0"
                              disabled={item?.discountType === 'NONE'}
                              {...register(`lineItems.${index}.discountValue`, {
                                valueAsNumber: true,
                              })}
                              className="w-full"
                            />
                          </TableCell>
                          <TableCell>
                            <Input
                              type="number"
                              step="0.01"
                              min="0"
                              max="100"
                              {...register(`lineItems.${index}.taxRate`, {
                                valueAsNumber: true,
                              })}
                              className="w-full"
                            />
                            {errors.lineItems?.[index]?.taxRate && (
                              <p className="text-xs text-red-600 mt-1">
                                {errors.lineItems[index]?.taxRate?.message}
                              </p>
                            )}
                          </TableCell>
                          <TableCell className="text-right font-medium">
                            {formatCurrency(lineTotal)}
                          </TableCell>
                          <TableCell>
                            {fields.length > 1 && (
                              <Button
                                type="button"
                                variant="ghost"
                                size="sm"
                                onClick={() => remove(index)}
                              >
                                Remove
                              </Button>
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Invoice'}
            </Button>
            <Button type="button" variant="outline" onClick={() => router.back()}>
              Cancel
            </Button>
          </div>
        </form>

        {/* Success Modal */}
        <Dialog open={successDialogOpen} onOpenChange={setSuccessDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Invoice Created Successfully!</DialogTitle>
              <DialogDescription>
                Your invoice has been created as a draft. Would you like to send it to the customer now?
              </DialogDescription>
            </DialogHeader>
            <DialogFooter className="flex-col sm:flex-row gap-2">
              <Button onClick={handleSendInvoice} disabled={sendingInvoice}>
                {sendingInvoice ? 'Sending...' : 'Send to Customer'}
              </Button>
              <Button variant="outline" onClick={handleViewInvoice}>
                View Invoice
              </Button>
              <Button variant="ghost" onClick={handleCreateAnother}>
                Create Another
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </Layout>
  );
}

