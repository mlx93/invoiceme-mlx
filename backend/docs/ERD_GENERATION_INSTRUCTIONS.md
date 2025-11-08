# ERD Diagram Generation Instructions

**Last Updated**: 2025-01-27

---

## Quick Start

To generate the ERD PNG diagram, use one of the following methods:

### Method 1: Python Script (Recommended)

1. **Install Graphviz**:
   ```bash
   # macOS
   brew install graphviz
   
   # Ubuntu/Debian
   sudo apt-get install graphviz
   
   # Windows
   # Download from https://graphviz.org/download/
   ```

2. **Install Python package**:
   ```bash
   pip install graphviz
   ```

3. **Run the script**:
   ```bash
   cd backend/docs
   python3 generate_erd.py
   ```

4. **Output**: `erd.png` will be generated in `backend/docs/`

---

### Method 2: dbdiagram.io (Online Tool)

1. Go to https://dbdiagram.io/
2. Create new diagram
3. Copy SQL from migration files (V1-V9) or manually create tables
4. Auto-generate relationships
5. Export as PNG: `File → Export → PNG`
6. Save as `backend/docs/erd.png`

---

### Method 3: pgAdmin (PostgreSQL Tool)

1. Connect to PostgreSQL database
2. Right-click database → `ERD Tool`
3. Add all tables:
   - customers
   - invoices
   - line_items
   - payments
   - users
   - recurring_invoice_templates
   - template_line_items
   - activity_feed
   - password_reset_tokens
4. Auto-generate relationships
5. Export as PNG: `File → Export → PNG`
6. Save as `backend/docs/erd.png`

---

### Method 4: DBeaver (Database Tool)

1. Connect to PostgreSQL database
2. Right-click database → `View Diagram`
3. Select all tables
4. Generate ERD
5. Export as PNG: `File → Export → Image`
6. Save as `backend/docs/erd.png`

---

## ERD Requirements

The ERD diagram should show:

1. **All 9 tables** with key fields labeled
2. **Foreign key relationships** with cardinality (1:N)
3. **Delete actions** (CASCADE, RESTRICT, SET NULL)
4. **Key fields** highlighted (PK, FK, UNIQUE)

---

## Current Status

- ✅ Text-based ERD created: `erd.md`
- ✅ Python script created: `generate_erd.py`
- ⏳ PNG diagram: Generate using one of the methods above

---

**Note**: The ERD PNG can be generated at any time using the methods above. The text-based ERD in `erd.md` provides a complete visual representation that can be used until the PNG is generated.

