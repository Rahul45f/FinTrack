# 💹 FinTrack — Personal Finance Tracker

A fully native Android app built with **Jetpack Compose** and **Kotlin** to track money you've lent or borrowed, manage your investment portfolio, and monitor your overall financial health — all stored locally on your device.

---

## 📱 Screenshots

> Coming soon

---

## ✨ Features

### 🔐 Security
- 4-digit PIN lock screen with a custom on-screen keypad
- First-time setup asks for your name and PIN
- Lock button to instantly secure the app
- All data stored locally — nothing leaves your device

### 🏠 Dashboard
- Account balance (tap to update)
- Quick overview of money to receive, money to give, and total portfolio P&L
- Recent pending people and active investment categories

### 👥 People & Ledger
- Search contacts by **name or phone number** with smart match ranking
- Add people directly from your phone contacts or manually
- Log money **lent** or **borrowed** with optional description and due date
- **Settle transactions** fully or partially — partial settle auto-creates a remaining transaction
- **Delete transactions** individually
- Transactions sorted newest first, settled ones shown dimmed
- People list sorted by highest outstanding balance
- People with zero balance are automatically hidden
- 🎉 "All settled up" screen when everything is cleared

### 📊 Investment Portfolio
- Track 6 categories: Stocks, Crypto, Mutual Funds, Fixed Deposits, Real Estate, Gold
- Per-category breakdown with invested amount, current value, and P&L %
- Total portfolio summary with overall P&L
- Add and delete individual holdings

### 🔔 Notifications
- Set a **due date** on any transaction
- App sends a notification on or after the due date as a payment reminder
- Overdue transactions shown in red

### 📱 Responsive Design
- Scales automatically across all screen sizes (small budget phones to large tablets)
- Dark and light theme with remembered preference

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM (ViewModel + State) |
| Storage | SharedPreferences via Gson |
| Async | Kotlin Coroutines + ViewModelScope |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Hedgehog or newer)
- JDK 17
- Android SDK 36

### Clone and Run

```bash
git clone https://github.com/YOUR_USERNAME/FinTrack.git
cd FinTrack
```

1. Open the project in **Android Studio**
2. Let Gradle sync complete
3. Connect a device or start an emulator
4. Press **▶ Run**

### Build APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

The debug APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Project Structure

```
app/src/main/java/com/fintrack/app/
├── MainActivity.kt        # Entry point, permission handling
├── AppVM.kt               # ViewModel — all state and business logic
├── Data.kt                # Data models (Transaction, Person, Investment) + Storage
├── Theme.kt               # Dark/Light color themes
├── Auth.kt                # PIN login and setup screens + shared UI helpers
├── Screens.kt             # All main screens (Dashboard, People, Portfolio, etc.)
├── Modals.kt              # Bottom sheet modals (add person, transaction, investment)
├── NotificationHelper.kt  # Due date notification logic
└── ContactsHelper.kt      # Phone contacts loading
```

---

## 🔒 Permissions

| Permission | Purpose |
|-----------|---------|
| `READ_CONTACTS` | Search and import from phone contacts |
| `POST_NOTIFICATIONS` | Send due date payment reminders |

Both are requested at runtime. The app works without them, just without contacts search and notifications.

---

## 💾 Data & Privacy

- All data is stored on-device using Android `SharedPreferences`
- No internet connection required
- No analytics, no tracking, no servers
- Clearing app data from Android settings will erase all FinTrack data

---

## 🤝 Contributing

Pull requests are welcome. For major changes please open an issue first to discuss what you'd like to change.

---

## 📄 License

```
MIT License

Copyright (c) 2025 Rahul45f

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 👤 Author

**Your Name**
- GitHub: [@YOUR_USERNAME](https://github.com/YOUR_USERNAME)
