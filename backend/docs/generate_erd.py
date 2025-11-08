#!/usr/bin/env python3
"""
Generate ERD diagram for InvoiceMe database schema.
Requires: graphviz (pip install graphviz)
"""

try:
    from graphviz import Digraph
except ImportError:
    print("Error: graphviz not installed. Install with: pip install graphviz")
    print("Also install Graphviz system package:")
    print("  macOS: brew install graphviz")
    print("  Ubuntu: sudo apt-get install graphviz")
    exit(1)

# Create directed graph
dot = Digraph(comment='InvoiceMe Database Schema', format='png')
dot.attr(rankdir='TB', size='12,16', dpi=300)
dot.attr('node', shape='box', style='rounded,filled', fillcolor='lightblue')

# Define tables with their columns
tables = {
    'customers': [
        'id (PK, UUID)',
        'company_name',
        'contact_name',
        'email (UNIQUE)',
        'phone',
        'street',
        'city',
        'state',
        'zip_code',
        'country',
        'customer_type',
        'credit_balance',
        'status',
        'created_at',
        'updated_at'
    ],
    'invoices': [
        'id (PK, UUID)',
        'invoice_number (UNIQUE)',
        'customer_id (FK)',
        'issue_date',
        'due_date',
        'status',
        'payment_terms',
        'subtotal',
        'tax_amount',
        'discount_amount',
        'total_amount',
        'amount_paid',
        'balance_due',
        'notes',
        'sent_date',
        'paid_date',
        'version',
        'created_at',
        'updated_at'
    ],
    'line_items': [
        'id (PK, UUID)',
        'invoice_id (FK)',
        'description',
        'quantity',
        'unit_price',
        'discount_type',
        'discount_value',
        'tax_rate',
        'sort_order',
        'created_at'
    ],
    'payments': [
        'id (PK, UUID)',
        'invoice_id (FK)',
        'customer_id (FK)',
        'amount',
        'payment_method',
        'payment_date',
        'payment_reference',
        'status',
        'created_by_user_id (FK)',
        'notes',
        'created_at'
    ],
    'users': [
        'id (PK, UUID)',
        'email (UNIQUE)',
        'password_hash',
        'full_name',
        'role',
        'customer_id (FK, nullable)',
        'status',
        'failed_login_count',
        'locked_until',
        'created_at',
        'updated_at'
    ],
    'recurring_invoice_templates': [
        'id (PK, UUID)',
        'customer_id (FK)',
        'template_name',
        'frequency',
        'start_date',
        'end_date',
        'next_invoice_date',
        'status',
        'payment_terms',
        'auto_send',
        'created_by_user_id (FK)',
        'created_at',
        'updated_at'
    ],
    'template_line_items': [
        'id (PK, UUID)',
        'template_id (FK)',
        'description',
        'quantity',
        'unit_price',
        'discount_type',
        'discount_value',
        'tax_rate',
        'sort_order'
    ],
    'activity_feed': [
        'id (PK, UUID)',
        'aggregate_id',
        'event_type',
        'description',
        'occurred_at',
        'user_id (FK, nullable)'
    ],
    'password_reset_tokens': [
        'id (PK, UUID)',
        'user_id (FK)',
        'token (UNIQUE)',
        'expires_at',
        'used',
        'created_at'
    ]
}

# Add tables to graph
for table_name, columns in tables.items():
    label = f'<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0">'
    label += f'<TR><TD COLSPAN="2" BGCOLOR="lightblue"><B>{table_name}</B></TD></TR>'
    for col in columns:
        label += f'<TR><TD ALIGN="LEFT">{col}</TD></TR>'
    label += '</TABLE>>'
    dot.node(table_name, label=label, shape='plaintext')

# Define relationships
relationships = [
    ('customers', 'invoices', 'customer_id', '1:N', 'RESTRICT'),
    ('customers', 'payments', 'customer_id', '1:N', 'RESTRICT'),
    ('customers', 'recurring_invoice_templates', 'customer_id', '1:N', 'RESTRICT'),
    ('customers', 'users', 'customer_id', '1:N', 'SET NULL'),
    ('invoices', 'line_items', 'invoice_id', '1:N', 'CASCADE'),
    ('invoices', 'payments', 'invoice_id', '1:N', 'RESTRICT'),
    ('users', 'payments', 'created_by_user_id', '1:N', 'SET NULL'),
    ('users', 'recurring_invoice_templates', 'created_by_user_id', '1:N', 'RESTRICT'),
    ('users', 'activity_feed', 'user_id', '1:N', 'SET NULL'),
    ('users', 'password_reset_tokens', 'user_id', '1:N', 'CASCADE'),
    ('recurring_invoice_templates', 'template_line_items', 'template_id', '1:N', 'CASCADE'),
]

# Add relationships to graph
for parent, child, fk, cardinality, delete_action in relationships:
    label = f'{fk}\n{cardinality}\n{delete_action}'
    dot.edge(parent, child, label=label, arrowhead='crow')

# Render graph
output_file = 'erd'
dot.render(output_file, cleanup=True)
print(f"ERD diagram generated: {output_file}.png")
print(f"ERD source file: {output_file}.gv")

