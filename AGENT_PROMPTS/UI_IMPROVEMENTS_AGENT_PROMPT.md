# UI Improvements Agent Prompt

## Context

You are a Frontend UI/UX Specialist working on the InvoiceMe ERP system. The application is built with Next.js 14, React, TypeScript, Tailwind CSS, and shadcn/ui components. The current UI is functional but needs professional polish and improved usability.

## Current Architecture

- **Framework**: Next.js 14 App Router
- **Styling**: Tailwind CSS
- **Components**: shadcn/ui component library
- **Charts**: Recharts library
- **File Structure**: Primary frontend code is in `frontend/app/` directory (not `frontend/src/app/`)
- **Layout**: Uses `Layout` component wrapper with `Header` component for navigation

## Required UI Improvements

### 1. Header Navigation Improvements

**File**: `frontend/src/components/layout/Header.tsx` (and `frontend/app/components/layout/Header.tsx` if it exists)

**Current Issues**:
- Tabs are centered/justified between logo and user menu
- Active tab styling is subtle (only color change to `text-blue-600`)
- No visual distinction for active state beyond color

**Required Changes**:
1. **Reposition Navigation Tabs**:
   - Move navigation tabs to start closer to the InvoiceMe logo (left side)
   - Reduce gap between logo and first tab
   - Use flexbox with `justify-start` instead of `justify-between` for nav items
   - Keep user menu (avatar dropdown) on the right side

2. **Enhanced Active Tab Styling**:
   - Add bottom border or underline for active tabs (2px solid, matching active color)
   - Increase font weight for active tabs (from `font-medium` to `font-semibold`)
   - Consider subtle background color or pill shape for active state
   - Add smooth transition effects on hover/active states
   - Use a more professional color scheme (consider using a primary brand color)

3. **Sleeker Header Design**:
   - Reduce padding if needed for tighter spacing
   - Add subtle shadow or border for depth
   - Ensure consistent spacing between tabs (use `gap-4` or `gap-6`)

**Example Active State Styling**:
```tsx
// Active tab should have:
className={`text-sm font-semibold border-b-2 border-blue-600 pb-1 ${
  pathname === '/dashboard' ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'
}`}
```

### 2. Invoice List Page Improvements

**File**: `frontend/app/invoices/page.tsx` (primary) and `frontend/src/app/invoices/page.tsx` (if exists)

**Current Issues**:
- Customer column shows `invoice.customerName` but appears empty (backend may not be populating it)
- Actions column with "View" button is redundant if rows are clickable
- No row hover effects or clickable indication

**Required Changes**:

1. **Make Invoice Rows Clickable**:
   - Remove the "Actions" column entirely
   - Make entire `TableRow` clickable to navigate to invoice detail page
   - Add `cursor-pointer` class to rows
   - Add hover effect (background color change, e.g., `hover:bg-gray-50`)
   - Use `onClick` handler on `TableRow` to navigate: `router.push(\`/invoices/${invoice.id}\`)`
   - Ensure accessibility: add `role="button"` and `tabIndex={0}` with keyboard handler

2. **Fix Customer Column**:
   - Verify backend is returning `customerName` in `InvoiceResponse`
   - Check `frontend/src/types/invoice.ts` - `InvoiceResponse` interface includes `customerName: string`
   - If backend is not populating it, check `ListInvoicesMapper.java` or similar mapper
   - Display customer company name prominently (use `font-medium` class)
   - Show fallback text if customerName is missing: `{invoice.customerName || 'Unknown Customer'}`

3. **Improve Table Styling**:
   - Add better spacing and padding
   - Ensure consistent alignment
   - Add subtle row separators if not already present

**Example Clickable Row**:
```tsx
<TableRow 
  key={invoice.id}
  className="cursor-pointer hover:bg-gray-50 transition-colors"
  onClick={() => router.push(`/invoices/${invoice.id}`)}
  role="button"
  tabIndex={0}
  onKeyDown={(e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      router.push(`/invoices/${invoice.id}`);
    }
  }}
>
  {/* cells */}
</TableRow>
```

### 3. Dashboard - Aging Report Table Improvements

**File**: `frontend/app/dashboard/page.tsx` (primary) and `frontend/src/app/dashboard/page.tsx` (if exists)

**Current Issues**:
- Uses plain HTML `<table>` instead of shadcn/ui `Table` component
- Basic styling, not professional looking
- No proper spacing, borders, or visual hierarchy

**Required Changes**:

1. **Replace with shadcn/ui Table Component**:
   - Import `Table`, `TableBody`, `TableCell`, `TableHead`, `TableHeader`, `TableRow` from `@/components/ui/table`
   - Replace plain HTML table with shadcn Table components
   - Maintain same data structure and formatting

2. **Professional Styling**:
   - Add proper header styling (bold, background color, or border)
   - Right-align numeric columns (Count, Amount)
   - Add consistent padding (`px-4 py-3` or similar)
   - Use alternating row colors for better readability (`even:bg-gray-50` or similar)
   - Add hover effects on rows
   - Ensure proper spacing between columns

3. **Formatting Improvements**:
   - Format currency values consistently (already using `formatCurrency`)
   - Add thousand separators if needed
   - Consider adding icons or visual indicators for age buckets (e.g., color coding: green for 0-30, yellow for 31-60, red for 90+)

**Example Table Structure**:
```tsx
<Table>
  <TableHeader>
    <TableRow>
      <TableHead className="font-semibold">Age Bucket</TableHead>
      <TableHead className="text-right font-semibold">Count</TableHead>
      <TableHead className="text-right font-semibold">Amount</TableHead>
    </TableRow>
  </TableHeader>
  <TableBody>
    {agingData.data.map((item) => (
      <TableRow key={item.bucket} className="hover:bg-gray-50">
        <TableCell className="font-medium">{item.bucket} days</TableCell>
        <TableCell className="text-right">{item.count}</TableCell>
        <TableCell className="text-right font-medium">
          {formatCurrency(item.amount.amount)}
        </TableCell>
      </TableRow>
    ))}
  </TableBody>
</Table>
```

### 4. Dashboard - Revenue Trend Chart Improvements

**File**: `frontend/app/dashboard/page.tsx` (primary) and `frontend/src/app/dashboard/page.tsx` (if exists)

**Current Issues**:
- Basic chart styling
- Y-axis doesn't format currency values
- Tooltip formatting could be improved
- No grid lines styling
- Chart colors could be more professional

**Required Changes**:

1. **Y-Axis Currency Formatting**:
   - Format Y-axis labels as currency (e.g., `$1K`, `$5K`, `$10K` for thousands)
   - Use `tickFormatter` on YAxis: `tickFormatter={(value) => formatCurrency(value)}`
   - Or use abbreviated format: `tickFormatter={(value) => \`$\${(value / 1000).toFixed(0)}K\`}` for large values

2. **Chart Styling**:
   - Improve color scheme (use professional blue gradient or brand colors)
   - Add rounded corners to bars if possible
   - Improve grid line styling (lighter, dashed)
   - Add better spacing and padding

3. **Tooltip Improvements**:
   - Ensure tooltip shows formatted currency
   - Add month label clearly
   - Consider custom tooltip component for better formatting
   - Add background color and border to tooltip

4. **X-Axis Improvements**:
   - Format month labels (e.g., "Jan 2024" instead of full date string)
   - Rotate labels if needed for readability
   - Add proper spacing

**Example Chart Configuration**:
```tsx
<BarChart data={revenueData.data}>
  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
  <XAxis 
    dataKey="month" 
    tickFormatter={(value) => {
      // Format month string to "MMM YYYY" format
      const date = new Date(value);
      return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
    }}
  />
  <YAxis 
    tickFormatter={(value) => formatCurrency(value)}
  />
  <Tooltip 
    formatter={(value: number) => formatCurrency(value)}
    contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '4px' }}
  />
  <Bar dataKey="revenue.amount" fill="#3b82f6" radius={[4, 4, 0, 0]} />
</BarChart>
```

## Implementation Guidelines

1. **Consistency**: Ensure all changes follow the existing code patterns and Tailwind CSS conventions
2. **Accessibility**: Maintain keyboard navigation, ARIA labels, and screen reader compatibility
3. **Responsive Design**: Ensure all improvements work on mobile devices (test at 375px viewport)
4. **Performance**: Avoid unnecessary re-renders, use proper React patterns
5. **Code Quality**: Follow TypeScript best practices, proper error handling

## Files to Modify

1. `frontend/src/components/layout/Header.tsx` - Header navigation improvements
2. `frontend/app/invoices/page.tsx` - Invoice list improvements (make rows clickable, fix customer column)
3. `frontend/app/dashboard/page.tsx` - Aging report table and revenue trend chart improvements

**Note**: If duplicate files exist in both `frontend/app/` and `frontend/src/app/`, update both to maintain consistency. The primary directory is `frontend/app/`.

## Testing Checklist

After implementing changes, verify:
- [ ] Header tabs are positioned closer to logo
- [ ] Active tabs have clear visual distinction (border, weight, color)
- [ ] Invoice rows are clickable and navigate to detail page
- [ ] Customer column displays company names correctly
- [ ] Actions column is removed from invoice list
- [ ] Aging report uses shadcn Table component with professional styling
- [ ] Revenue trend chart has formatted Y-axis and improved tooltips
- [ ] All changes work on mobile (375px viewport)
- [ ] Keyboard navigation works for clickable rows
- [ ] No console errors or warnings

## Backend Investigation (If Needed)

If customer names are not displaying:
1. Check `backend/src/main/java/com/invoiceme/invoices/listinvoices/ListInvoicesMapper.java`
2. Verify `InvoiceResponse` includes `customerName` field
3. Check if `Invoice` entity has proper relationship to `Customer` entity
4. Verify mapper is populating `customerName` from `invoice.getCustomer().getCompanyName()`

## Expected Outcome

A professional, polished UI with:
- Sleek header with clearly visible active tab indicators
- Clickable invoice rows for better UX
- Properly displayed customer information
- Professional dashboard tables and charts
- Consistent styling throughout the application

