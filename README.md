# JK Chicken Center - Billing & Inventory Management System

A complete desktop billing and inventory management application for Indian chicken centers, built with Java, JavaFX, and SQLite.

## Features

### Core Features
- **Billing System**: Create bills, add items, calculate totals, complete sales
- **Inventory Management**: Track stock levels, manage items, view current inventory
- **Purchase Management**: Record purchases from vendors, update stock automatically
- **Batch-wise Tracking**: Track inventory by purchase batches
- **Pricing Management**: Set and update item prices
- **Profit/Loss Tracking**: View financial reports and profit analysis

### Expense Management
- **Vendor Expenses**: Record and manage vendor-related expenses
- **Employee Expenses**: Track employee payments and expenses

### Additional Features
- **Vendor Management**: CRUD operations for vendors
- **Employee Management**: CRUD operations for employees
- **Financial Reports**: Sales, purchases, expenses, and profit reports
- **Real-time Stock Updates**: Stock auto-updates on sales and purchases

## Tech Stack

- **Java 17**: Core application logic
- **JavaFX 17**: Modern desktop UI
- **SQLite**: Serverless database
- **JDBC**: Database connectivity

## Project Structure

```
src/main/java/com/chickencenter/
├── model/          # Data models (Vendor, Item, Sale, etc.)
├── dao/            # Data Access Objects (CRUD operations)
├── service/        # Business logic layer
├── database/       # Database connection and initialization
├── ui/
│   └── controllers/ # JavaFX controllers
└── util/           # Utility classes

src/main/resources/com/chickencenter/ui/
├── dashboard.fxml  # Main dashboard
├── billing.fxml     # Billing screen
├── inventory.fxml   # Inventory management
├── purchase.fxml    # Purchase management
├── vendors.fxml     # Vendor management
├── employees.fxml   # Employee management
├── expenses.fxml    # Expense management
└── reports.fxml     # Financial reports
```

## Database Schema

### Tables
- `vendors` - Vendor information
- `items` - Product items
- `purchase_batches` - Purchase batch tracking
- `price_list` - Item pricing history
- `sales` - Sales records
- `sale_items` - Individual sale items
- `vendor_expenses` - Vendor expenses
- `employees` - Employee records
- `employee_expenses` - Employee expenses
- `stock` - Current stock levels

## Build & Run

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean package
```

### Run
```bash
mvn javafx:run
```

Or run the JAR file:
```bash
java -jar target/jk-chicken-center-1.0.0.jar
```

## Sample Workflow

### Create a Sale
1. Open **Billing** from dashboard
2. Select item from available items
3. Enter quantity and click **ADD**
4. Review cart and total
5. Click **COMPLETE SALE** to finalize

### Purchase Stock
1. Open **Purchase** from dashboard
2. Select vendor and item
3. Enter quantity and price
4. Click **MAKE PURCHASE**
5. Stock automatically updates

### Record Expense
1. Open **Expenses** from dashboard
2. Select Vendor or Employee tab
3. Fill in details
4. Click **ADD EXPENSE**

## UI Theme

The application uses a professional dark theme:
- Primary Background: `#1a1a2e`
- Secondary Background: `#16213e`
- Card Background: `#0f3460`
- Accent Color: `#e94560`
- Success Color: `#4caf50`
- Warning Color: `#ff9800`

## Business Rules

1. **Stock Validation**: Cannot sell more than available stock
2. **Transaction Atomicity**: All related operations are transactional
3. **Auto Price**: Prices auto-populate from latest price list
4. **Stock Sync**: Stock updates automatically on sales/purchases

## Future Enhancements

- [ ] User authentication
- [ ] Invoice printing
- [ ] SMS notifications
- [ ] Barcode support
- [ ] Multi-branch support
- [ ] Cloud sync
- [ ] Advanced analytics

## License

This project is proprietary software for JK Chicken Center.
