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
import { useCreateRecurringInvoice } from '@/hooks/useRecurringInvoices';
import { useCustomers } from '@/hooks/useCustomers';
import { useAuth } from '@/contexts/AuthContext';
import { canManageRecurringInvoices } from '@/lib/rbac';
import { RecurringFrequency, PaymentTerms, DiscountType } from '@/types/common';

const lineItemSchema = z.object({
  description: z.string().min(1, 'Description is required'),
  quantity: z.number().min(1, 'Quantity must be at least 1'),
  unitPrice: z.number().min(0, 'Unit price must be 0 or greater'),
  discountType: z.enum(['NONE', 'PERCENTAGE', 'FIXED']),
  discountValue: z.number().min(0).optional(),
  taxRate: z.number().min(0).max(100, 'Tax rate must be between 0 and 100'),
});

const templateSchema = z.object({
  customerId: z.string().uuid('Please select a customer'),
  templateName: z.string().min(1, 'Template name is required'),
  frequency: z.enum(['MONTHLY', 'QUARTERLY', 'ANNUALLY']),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().optional(),
  paymentTerms: z.enum(['NET_30', 'DUE_ON_RECEIPT', 'CUSTOM']),
  autoSend: z.boolean(),
  lineItems: z.array(lineItemSchema).min(1, 'At least one line item is required'),
});

type TemplateFormData = z.infer<typeof templateSchema>;

export default function CreateRecurringInvoicePage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { createTemplate, loading, error } = useCreateRecurringInvoice();
  const { customers } = useCustomers({ size: 1000 });

  const {
    register,
    handleSubmit,
    control,
    watch,
    setValue,
    formState: { errors },
  } = useForm<TemplateFormData>({
    resolver: zodResolver(templateSchema),
    defaultValues: {
      frequency: 'MONTHLY',
      paymentTerms: 'NET_30',
      autoSend: false,
      startDate: new Date().toISOString().split('T')[0],
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

  const lineItems = watch('lineItems');
  const autoSend = watch('autoSend');

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canManageRecurringInvoices(user.role)) {
      router.push('/dashboard');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const onSubmit = async (data: TemplateFormData) => {
    try {
      const template = await createTemplate({
        customerId: data.customerId,
        templateName: data.templateName,
        frequency: data.frequency,
        startDate: data.startDate,
        endDate: data.endDate,
        paymentTerms: data.paymentTerms,
        autoSend: data.autoSend,
        lineItems: data.lineItems.map((item) => ({
          description: item.description,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          discountType: item.discountType,
          discountValue: item.discountValue || 0,
          taxRate: item.taxRate,
        })),
      });
      router.push(`/recurring-invoices/${template.id}`);
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

  return (
    <Layout>
      <div className="max-w-4xl mx-auto space-y-6">
        <h1 className="text-3xl font-bold">Create Recurring Invoice Template</h1>

        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Template Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <Label htmlFor="templateName">Template Name *</Label>
                <Input
                  id="templateName"
                  placeholder="e.g., Monthly Storage Fee"
                  {...register('templateName')}
                  className="mt-1"
                />
                {errors.templateName && (
                  <p className="mt-1 text-sm text-red-600">{errors.templateName.message}</p>
                )}
              </div>

              <div>
                <Label htmlFor="customerId">Customer *</Label>
                <Select onValueChange={(value) => setValue('customerId', value)}>
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

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="frequency">Frequency *</Label>
                  <Select
                    onValueChange={(value) => setValue('frequency', value as RecurringFrequency)}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MONTHLY">Monthly</SelectItem>
                      <SelectItem value="QUARTERLY">Quarterly</SelectItem>
                      <SelectItem value="ANNUALLY">Annually</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="startDate">Start Date *</Label>
                  <Input
                    id="startDate"
                    type="date"
                    {...register('startDate')}
                    className="mt-1"
                  />
                  {errors.startDate && (
                    <p className="mt-1 text-sm text-red-600">{errors.startDate.message}</p>
                  )}
                </div>
              </div>

              <div>
                <Label htmlFor="endDate">End Date (Optional)</Label>
                <Input
                  id="endDate"
                  type="date"
                  {...register('endDate')}
                  className="mt-1"
                />
              </div>

              <div>
                <Label htmlFor="paymentTerms">Payment Terms *</Label>
                <Select
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

              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="autoSend"
                  {...register('autoSend')}
                  className="rounded"
                />
                <Label htmlFor="autoSend" className="cursor-pointer">
                  Auto-send invoices when generated
                </Label>
              </div>
            </CardContent>
          </Card>

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
                      <TableHead className="w-16"></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {fields.map((field, index) => (
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
                        </TableCell>
                        <TableCell>
                          <Select
                            value={lineItems[index]?.discountType || 'NONE'}
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
                            disabled={lineItems[index]?.discountType === 'NONE'}
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
                    ))}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Template'}
            </Button>
            <Button type="button" variant="outline" onClick={() => router.back()}>
              Cancel
            </Button>
          </div>
        </form>
      </div>
    </Layout>
  );
}

