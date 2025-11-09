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
    <header className="border-b bg-white shadow-sm">
      <div className="container mx-auto px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <Link href="/dashboard" className="text-xl font-bold text-gray-900">
            InvoiceMe
          </Link>

          <nav className="flex items-center gap-2">
            {user && (
              <>
                {user.role === 'CUSTOMER' ? (
                  <Link
                    href="/customer-portal"
                    className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                      pathname === '/customer-portal'
                        ? 'bg-gray-900 text-white'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    Portal
                  </Link>
                ) : (
                  <Link
                    href="/dashboard"
                    className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                      pathname === '/dashboard' 
                        ? 'bg-gray-900 text-white' 
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    Dashboard
                  </Link>
                )}
                {user.role !== 'CUSTOMER' && (
                  <>
                    <Link
                      href="/customers"
                      className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                        pathname?.startsWith('/customers')
                          ? 'bg-gray-900 text-white'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                    >
                      Customers
                    </Link>
                    <Link
                      href="/invoices"
                      className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                        pathname?.startsWith('/invoices')
                          ? 'bg-gray-900 text-white'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                    >
                      Invoices
                    </Link>
                    <Link
                      href="/payments"
                      className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                        pathname?.startsWith('/payments')
                          ? 'bg-gray-900 text-white'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                      }`}
                    >
                      Payments
                    </Link>
                    {user.role === 'SYSADMIN' && (
                      <Link
                        href="/users/pending"
                        className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                          pathname?.startsWith('/users')
                            ? 'bg-gray-900 text-white'
                            : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
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
                    className={`text-sm font-medium px-4 py-2 rounded-md transition-all ${
                      pathname?.startsWith('/invoices')
                        ? 'bg-gray-900 text-white'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    My Invoices
                  </Link>
                )}
              </>
            )}
          </nav>
        </div>

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
      </div>
    </header>
  );
}

