'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { useAuth } from '@/contexts/AuthContext';
import { useLogout } from '@/hooks/useAuth';

export function Header() {
  const pathname = usePathname();
  const { user } = useAuth();
  const logout = useLogout();

  // Don't show header on login/register pages
  if (pathname === '/login' || pathname === '/register') {
    return null;
  }

  const userInitials = user?.fullName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || 'U';

  return (
    <header className="border-b bg-white">
      <div className="container mx-auto px-4 py-4 flex items-center justify-between">
        <Link href="/dashboard" className="text-xl font-bold text-gray-900">
          InvoiceMe
        </Link>

        <nav className="flex items-center gap-6">
          {user && (
            <>
              {user.role === 'CUSTOMER' ? (
                <Link
                  href="/customer-portal"
                  className={`text-sm font-medium ${
                    pathname === '/customer-portal'
                      ? 'text-blue-600'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  Portal
                </Link>
              ) : (
                <Link
                  href="/dashboard"
                  className={`text-sm font-medium ${
                    pathname === '/dashboard' ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  Dashboard
                </Link>
              )}
              {user.role !== 'CUSTOMER' && (
                <>
                  <Link
                    href="/customers"
                    className={`text-sm font-medium ${
                      pathname?.startsWith('/customers')
                        ? 'text-blue-600'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    Customers
                  </Link>
                  <Link
                    href="/invoices"
                    className={`text-sm font-medium ${
                      pathname?.startsWith('/invoices')
                        ? 'text-blue-600'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    Invoices
                  </Link>
                  <Link
                    href="/payments"
                    className={`text-sm font-medium ${
                      pathname?.startsWith('/payments')
                        ? 'text-blue-600'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    Payments
                  </Link>
                  {user.role === 'SYSADMIN' && (
                    <Link
                      href="/users/pending"
                      className={`text-sm font-medium ${
                        pathname?.startsWith('/users')
                          ? 'text-blue-600'
                          : 'text-gray-600 hover:text-gray-900'
                      }`}
                    >
                      Users
                    </Link>
                  )}
                </>
              )}
              {user.role === 'CUSTOMER' && (
                <Link
                  href="/invoices"
                  className={`text-sm font-medium ${
                    pathname?.startsWith('/invoices')
                      ? 'text-blue-600'
                      : 'text-gray-600 hover:text-gray-900'
                  }`}
                >
                  My Invoices
                </Link>
              )}
            </>
          )}

          {user && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarFallback>{userInitials}</AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end">
                <div className="px-2 py-1.5 text-sm">
                  <div className="font-medium">{user.fullName}</div>
                  <div className="text-xs text-gray-500">{user.email}</div>
                  <div className="text-xs text-gray-500 mt-1">{user.role}</div>
                </div>
                <DropdownMenuItem onClick={logout}>Log out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </nav>
      </div>
    </header>
  );
}

