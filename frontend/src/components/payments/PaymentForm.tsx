'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useRecordPayment } from '@/hooks/usePayments';
import { PaymentMethod } from '@/types/common';
import { formatCurrency } from '@/lib/utils';

const paymentSchema = z.object({
  amount: z.number().min(0.01, 'Amount must be greater than 0'),
  paymentMethod: z.enum(['CREDIT_CARD', 'ACH']),
  paymentDate: z.string().min(1, 'Payment date is required'),
  paymentReference: z.string().optional(),
  notes: z.string().optional(),
});

type PaymentFormData = z.infer<typeof paymentSchema>;

interface PaymentFormProps {
  invoiceId: string;
  balanceDue: number;
  onSuccess?: () => void;
}

export function PaymentForm({ invoiceId, balanceDue, onSuccess }: PaymentFormProps) {
  const { recordPayment, loading, error } = useRecordPayment();
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CREDIT_CARD');

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<PaymentFormData>({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      amount: balanceDue,
      paymentMethod: 'CREDIT_CARD',
      paymentDate: new Date().toISOString().split('T')[0],
    },
  });

  const amount = watch('amount');

  const onSubmit = async (data: PaymentFormData) => {
    try {
      await recordPayment({
        invoiceId,
        amount: data.amount,
        paymentMethod: data.paymentMethod,
        paymentDate: data.paymentDate,
        paymentReference: data.paymentReference,
        notes: data.notes,
      });
      onSuccess?.();
    } catch (err) {
      // Error handled by hook
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div>
        <Label htmlFor="amount">Amount *</Label>
        <Input
          id="amount"
          type="number"
          step="0.01"
          min="0.01"
          max={balanceDue}
          {...register('amount', { valueAsNumber: true })}
          className="mt-1"
        />
        {errors.amount && (
          <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
        )}
        <p className="mt-1 text-xs text-gray-500">
          Balance due: {formatCurrency(balanceDue)}
        </p>
      </div>

      <div>
        <Label htmlFor="paymentMethod">Payment Method *</Label>
        <Select
          value={paymentMethod}
          onValueChange={(value) => {
            setPaymentMethod(value as PaymentMethod);
            setValue('paymentMethod', value as PaymentMethod);
          }}
        >
          <SelectTrigger className="mt-1">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="CREDIT_CARD">Credit Card</SelectItem>
            <SelectItem value="ACH">ACH</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div>
        <Label htmlFor="paymentDate">Payment Date *</Label>
        <Input
          id="paymentDate"
          type="date"
          {...register('paymentDate')}
          className="mt-1"
        />
        {errors.paymentDate && (
          <p className="mt-1 text-sm text-red-600">{errors.paymentDate.message}</p>
        )}
      </div>

      <div>
        <Label htmlFor="paymentReference">Payment Reference</Label>
        <Input
          id="paymentReference"
          type="text"
          placeholder="e.g., VISA-4532"
          {...register('paymentReference')}
          className="mt-1"
        />
      </div>

      <div>
        <Label htmlFor="notes">Notes</Label>
        <Textarea
          id="notes"
          placeholder="Optional notes about this payment"
          {...register('notes')}
          className="mt-1"
        />
      </div>

      {amount > balanceDue && (
        <Alert>
          <AlertDescription>
            Payment amount exceeds balance due. Excess amount ({formatCurrency(amount - balanceDue)}) will be applied as customer credit.
          </AlertDescription>
        </Alert>
      )}

      <div className="flex gap-2 pt-4">
        <Button type="submit" className="flex-1" disabled={loading}>
          {loading ? 'Recording...' : 'Record Payment'}
        </Button>
      </div>
    </form>
  );
}

