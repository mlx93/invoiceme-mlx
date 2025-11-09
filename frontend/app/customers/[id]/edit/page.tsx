'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useCustomer, useUpdateCustomer } from '@/hooks/useCustomers';
import { useAuth } from '@/contexts/AuthContext';
import { canEditCustomer } from '@/lib/rbac';
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

export default function EditCustomerPage() {
  const router = useRouter();
  const params = useParams();
  const customerId = params?.id as string;
  
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { customer, loading: loadingCustomer, error: fetchError } = useCustomer(customerId);
  const { updateCustomer, loading: updating, error: updateError } = useUpdateCustomer();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
  });

  const customerType = watch('customerType');

  // Populate form with existing customer data
  useEffect(() => {
    if (customer) {
      reset({
        companyName: customer.companyName || '',
        contactName: customer.contactName || '',
        email: customer.email || '',
        phone: customer.phone || '',
        street: customer.address?.street || '',
        city: customer.address?.city || '',
        state: customer.address?.state || '',
        zipCode: customer.address?.zipCode || '',
        country: customer.address?.country || 'USA',
        customerType: customer.customerType || 'COMMERCIAL',
      });
    }
  }, [customer, reset]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    } else if (user && !canEditCustomer(user.role)) {
      router.push('/customers');
    }
  }, [authLoading, isAuthenticated, user, router]);

  const onSubmit = async (data: CustomerFormData) => {
    try {
      // Only include address if at least one field is filled
      const hasAddress = data.street || data.city || data.state || data.zipCode || data.country;
      
      await updateCustomer(customerId, {
        companyName: data.companyName,
        contactName: data.contactName,
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
      router.push(`/customers/${customerId}`);
    } catch (err) {
      // Error handled by hook
    }
  };

  if (authLoading || loadingCustomer) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  if (fetchError) {
    return (
      <Layout>
        <div className="max-w-2xl mx-auto space-y-6">
          <Alert variant="destructive">
            <AlertDescription>{fetchError}</AlertDescription>
          </Alert>
          <Button onClick={() => router.back()}>Go Back</Button>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto space-y-6">
        <h1 className="text-3xl font-bold">Edit Customer</h1>

        {updateError && (
          <Alert variant="destructive">
            <AlertDescription>{updateError}</AlertDescription>
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
              <Input id="email" type="email" {...register('email')} className="mt-1" disabled />
              <p className="mt-1 text-sm text-gray-500">Email cannot be changed</p>
            </div>

            <div>
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" type="tel" {...register('phone')} className="mt-1" />
            </div>

            <div>
              <Label htmlFor="customerType">Customer Type *</Label>
              <Select
                value={customerType || 'COMMERCIAL'}
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
                <Input placeholder="Country" {...register('country')} />
              </div>
            </div>
          </div>

          <div className="flex gap-4">
            <Button type="submit" disabled={updating}>
              {updating ? 'Updating...' : 'Update Customer'}
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

