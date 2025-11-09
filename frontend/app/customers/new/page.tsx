'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useCreateCustomer } from '@/hooks/useCustomers';
import { useAuth } from '@/contexts/AuthContext';
import { canCreateCustomer } from '@/lib/rbac';
import { CustomerType } from '@/types/common';

const customerSchema = z.object({
  companyName: z.string().min(1, 'Company name is required'),
  contactName: z.string().optional(),
  email: z.string().email('Invalid email address'),
  phone: z.string().optional(),
  street: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  zipCode: z.string().optional(),
  country: z.string().optional(),
  customerType: z.enum(['RESIDENTIAL', 'COMMERCIAL', 'INSURANCE']),
});

type CustomerFormData = z.infer<typeof customerSchema>;

export default function CreateCustomerPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { createCustomer, loading, error } = useCreateCustomer();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: {
      customerType: 'COMMERCIAL',
      country: 'USA',
    },
  });

  const customerType = watch('customerType');

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canCreateCustomer(user.role)) {
      router.push('/customers');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const onSubmit = async (data: CustomerFormData) => {
    try {
      // Only include address if at least one field is filled
      const hasAddress = data.street || data.city || data.state || data.zipCode || data.country;
      
      const customer = await createCustomer({
        companyName: data.companyName,
        contactName: data.contactName,
        email: data.email,
        phone: data.phone,
        customerType: data.customerType,
        ...(hasAddress && {
          address: {
            street: data.street,
            city: data.city,
            state: data.state,
            zipCode: data.zipCode,
            country: data.country,
          },
        }),
      });
      router.push(`/customers/${customer.id}`);
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
      <div className="max-w-2xl mx-auto space-y-6">
        <h1 className="text-3xl font-bold">Create Customer</h1>

        {error && (
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-4">
            <div>
              <Label htmlFor="companyName">Company Name *</Label>
              <Input id="companyName" {...register('companyName')} className="mt-1" />
              {errors.companyName && (
                <p className="mt-1 text-sm text-red-600">{errors.companyName.message}</p>
              )}
            </div>

            <div>
              <Label htmlFor="contactName">Contact Name</Label>
              <Input id="contactName" {...register('contactName')} className="mt-1" />
            </div>

            <div>
              <Label htmlFor="email">Email *</Label>
              <Input id="email" type="email" {...register('email')} className="mt-1" />
              {errors.email && (
                <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>

            <div>
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" type="tel" {...register('phone')} className="mt-1" />
            </div>

            <div>
              <Label htmlFor="customerType">Customer Type *</Label>
              <Select
                value={customerType}
                onValueChange={(value) => setValue('customerType', value as CustomerType)}
              >
                <SelectTrigger className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="RESIDENTIAL">Residential</SelectItem>
                  <SelectItem value="COMMERCIAL">Commercial</SelectItem>
                  <SelectItem value="INSURANCE">Insurance</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Address</Label>
              <Input placeholder="Street" {...register('street')} />
              <div className="grid grid-cols-2 gap-2">
                <Input placeholder="City" {...register('city')} />
                <Input placeholder="State" {...register('state')} />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <Input placeholder="ZIP Code" {...register('zipCode')} />
                <Input placeholder="Country" {...register('country')} defaultValue="USA" />
              </div>
            </div>
          </div>

          <div className="flex gap-4">
            <Button type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Customer'}
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

