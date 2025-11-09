'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Layout } from '@/components/layout/Layout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { LoadingModal } from '@/components/ui/loading-modal';
import { useDashboardMetrics, useInvoiceStatus, useAgingReport } from '@/hooks/useDashboard';
import { useAuth } from '@/contexts/AuthContext';
import { formatCurrency } from '@/lib/utils';

export default function DashboardPage() {
  const router = useRouter();
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();
  const { metrics, loading: metricsLoading } = useDashboardMetrics();
  const { data: statusData, loading: statusLoading } = useInvoiceStatus();
  const { data: agingData, loading: agingLoading } = useAgingReport();

  // Combine all loading states
  const isLoading = metricsLoading || statusLoading || agingLoading;

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    // Customer role redirects to dedicated portal
    if (!authLoading && isAuthenticated && user?.role === 'CUSTOMER') {
      router.push('/customer-portal');
    }
  }, [authLoading, isAuthenticated, user?.role, router]);

  if (authLoading || !isAuthenticated) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </Layout>
    );
  }

  // Show loading state while redirecting customers
  if (user?.role === 'CUSTOMER') {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Redirecting to customer portal...</div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <LoadingModal isLoading={isLoading} />
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold">Dashboard</h1>
        </div>

        {/* Metrics Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-lg font-semibold leading-tight">Revenue (MTD)</CardTitle>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="text-3xl font-bold">
                {metrics?.totalRevenueMTD?.amount ? formatCurrency(metrics.totalRevenueMTD.amount) : '$0.00'}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-1">
              <div className="flex items-start gap-2">
                <CardTitle className="text-lg font-semibold leading-tight flex-1">Outstanding Invoices</CardTitle>
                <div className="border border-gray-900 rounded px-2 py-1">
                  <span className="text-3xl font-bold">{metrics?.outstandingInvoicesCount ?? 0}</span>
                </div>
              </div>
            </CardHeader>
            <CardContent className="pt-1">
              <div className="text-3xl font-bold">
                {metrics?.outstandingInvoicesAmount?.amount ? formatCurrency(metrics.outstandingInvoicesAmount.amount) : '$0.00'}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-1">
              <div className="flex items-start gap-2">
                <CardTitle className="text-lg font-semibold leading-tight flex-1">Overdue Invoices</CardTitle>
                <div className="border border-gray-900 rounded px-2 py-1">
                  <span className="text-3xl font-bold">{metrics?.overdueInvoicesCount ?? 0}</span>
                </div>
              </div>
            </CardHeader>
            <CardContent className="pt-1">
              <div className="text-3xl font-bold">
                {metrics?.overdueInvoicesAmount?.amount ? formatCurrency(metrics.overdueInvoicesAmount.amount) : '$0.00'}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-lg font-semibold leading-tight">Active Customers</CardTitle>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="text-3xl font-bold">{metrics?.activeCustomers ?? 0}</div>
            </CardContent>
          </Card>
        </div>

        {/* Aging Report and Status */}
        <div className="grid gap-3 md:grid-cols-2">
          {/* Aging Report - Compact */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Aging Report</CardTitle>
              <CardDescription className="text-sm">Outstanding invoices by age</CardDescription>
            </CardHeader>
            <CardContent>
              {agingLoading ? (
                <div className="h-[200px] flex items-center justify-center text-sm">Loading...</div>
              ) : agingData?.data && agingData.data.length > 0 ? (
                <div className="space-y-3">
                  {agingData.data.map((item) => {
                    // Determine color coding based on age bucket
                    const bucketNumber = parseInt(item.bucket.split('-')[0]) || 0;
                    let bgGradient = '';
                    let borderColor = '';
                    
                    if (bucketNumber >= 90) {
                      bgGradient = 'bg-red-50';
                      borderColor = 'border-l-red-500';
                    } else if (bucketNumber >= 61) {
                      bgGradient = 'bg-orange-50';
                      borderColor = 'border-l-orange-500';
                    } else if (bucketNumber >= 31) {
                      bgGradient = 'bg-yellow-50';
                      borderColor = 'border-l-yellow-500';
                    } else {
                      bgGradient = 'bg-green-50';
                      borderColor = 'border-l-green-500';
                    }
                    
                    return (
                      <div 
                        key={item.bucket} 
                        className={`${bgGradient} ${borderColor} border-l-4 rounded-md p-3 flex items-center hover:shadow-sm transition-all mb-2`}
                      >
                        <div className="flex items-center gap-4 flex-1 py-1">
                          <div className="text-sm font-bold uppercase tracking-wide text-gray-700 min-w-[100px]">
                            {item.bucket} days
                          </div>
                          <div className="text-xl font-bold text-gray-900">
                            {formatCurrency(item.amount.amount)}
                          </div>
                        </div>
                        <div className="flex items-center gap-1.5 ml-auto mr-8">
                          <div className="text-sm text-gray-500 uppercase font-semibold">Invoices</div>
                          <div className="text-lg font-bold text-gray-700">
                            {item.count}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="h-[200px] flex flex-col items-center justify-center text-gray-500">
                  <div className="w-12 h-12 mb-3 rounded-full bg-green-100 flex items-center justify-center">
                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <p className="text-sm font-medium">No Outstanding Invoices</p>
                  <p className="text-xs mt-1">All invoices are paid or cancelled</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Invoice Status Breakdown */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Invoice Status Breakdown</CardTitle>
              <CardDescription className="text-sm">Distribution of invoices by status</CardDescription>
            </CardHeader>
            <CardContent>
              {statusLoading ? (
                <div className="h-[200px] flex items-center justify-center">Loading...</div>
              ) : statusData?.data ? (
                <div className="space-y-2">
                  {statusData.data
                    .filter((item) => item.status !== 'CANCELLED')
                    .map((item) => {
                    // Calculate percentage
                    const totalCount = statusData.data
                      .filter((i) => i.status !== 'CANCELLED')
                      .reduce((sum, i) => sum + i.count, 0);
                    const percentage = totalCount > 0 ? (item.count / totalCount) * 100 : 0;
                    
                    // Color coding by status
                    let bgColor = '';
                    let barColor = '';
                    
                    if (item.status === 'PAID') {
                      bgColor = 'bg-green-50';
                      barColor = 'bg-green-500';
                    } else if (item.status === 'SENT') {
                      bgColor = 'bg-blue-50';
                      barColor = 'bg-blue-500';
                    } else if (item.status === 'OVERDUE') {
                      bgColor = 'bg-red-50';
                      barColor = 'bg-red-500';
                    } else if (item.status === 'DRAFT') {
                      bgColor = 'bg-gray-50';
                      barColor = 'bg-gray-500';
                    }
                    
                    return (
                      <div 
                        key={item.status} 
                        className={`${bgColor} rounded-md p-2.5 hover:shadow-sm transition-all`}
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-4">
                            <span className="text-sm font-bold uppercase tracking-wide text-gray-700 min-w-[90px]">
                              {item.status}
                            </span>
                            <span className="text-xl font-bold text-gray-900">
                              {item.count}
                            </span>
                          </div>
                          <div className="text-lg font-bold text-gray-700">
                            {formatCurrency(item.amount.amount)}
                          </div>
                        </div>
                        
                        {/* Progress bar */}
                        <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
                          <div 
                            className={`${barColor} h-1.5 rounded-full transition-all duration-500`}
                            style={{ width: `${percentage}%` }}
                          ></div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div className="h-[200px] flex items-center justify-center text-gray-500">
                  No data available
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </Layout>
  );
}

