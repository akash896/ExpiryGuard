# ExpiryGuard

ExpiryGuard is an Android app for tracking the expiry dates, value, and lifecycle of household items. It is designed for food, medicines, documents, warranties, cosmetics, subscriptions, and other items that need attention before they expire.

## Current features

- Firebase Anonymous Authentication creates or reuses a user session at app start.
- Cloud Firestore stores each user's data at `users/{userId}/items/{itemId}`.
- Home groups active items by expiry urgency and provides search, category filters, and value summaries.
- Add/Edit supports name, category, expiry date, optional purchase date, quantity, price, currency, reminder period, and notes.
- Expense Insights groups spending and value by daily, monthly, quarterly, or annual purchase periods.
- Item Detail shows all saved fields, expiry timing, created/updated timestamps, value state, and lifecycle actions.
- An item can be marked consumed, restored to not consumed, archived, or deleted. Archiving and deleting require confirmation.
- Navigation uses explicit routes for Home, Add Item, Edit Item, Item Detail, Settings, and Expense Insights. Screens other than Home provide a back action in the top app bar.

## Technology

- Kotlin and Jetpack Compose with Material 3
- MVVM and a repository abstraction
- Navigation Compose
- Firebase Authentication and Cloud Firestore, managed through the Firebase BoM
- Kotlin coroutines and `Flow`
- WorkManager is configured as a dependency for the forthcoming daily reminder worker
- Minimum Android SDK: 26
- Application ID: `com.akash.expiryguard`

## Data model

An `ExpiryItem` contains the item identity, category, ISO dates (`yyyy-MM-dd`), quantity, price, currency, notes, reminder preference, timestamps, and archive/consumption state. Price defaults to `0.0` and currency defaults to `INR`.

Expiry state is calculated from `expiryDate`:

- Expired: before today and not consumed
- Expiring today: today
- Expiring this week: 1 to 7 days away
- Expiring this month: 8 to 30 days away
- Safe for later: more than 30 days away

Expense summaries use `purchaseDate`; when it is absent or invalid, they use `expiryDate` as a fallback. Expired value includes unconsumed expired items, consumed value includes consumed items, and active value includes unconsumed non-expired items.

## Firebase setup

The project expects Firebase to be configured for the application ID above.

1. Place the Firebase configuration file at `app/google-services.json`.
2. Enable Anonymous Authentication in Firebase Authentication.
3. Create a Cloud Firestore database.
4. Apply rules that allow an authenticated user to access only documents inside their own `users/{userId}` document.

No Firebase API keys are written in source code. The Google Services Gradle plugin reads them from `google-services.json` during the build.

## Build and run

Open this directory in Android Studio, allow Gradle to sync, choose an Android 8.0+ device or emulator, and press Run.

From a terminal at the repository root:

```bash
./gradlew :app:assembleDebug
```

Install the resulting APK on a connected emulator or device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Run the focused JVM tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Project layout

```text
app/src/main/java/com/akash/expiryguard/
  data/model/          Item, category, status, and expense models
  data/repository/     Repository contract
  data/firebase/       Firebase Auth and Firestore implementation
  ui/screens/          Home, Add/Edit, Detail, Insights, and Settings screens
  ui/navigation/       Compose routes and navigation host
  util/                Date, expense, and Firebase task helpers
  notifications/       Reserved for the reminder worker and notification channel
```

## Navigation

Route constants live in `ui/navigation/AppRoutes.kt`. The navigation graph in `ui/navigation/AppNavGraph.kt` provides these routes:

- `home`
- `add_item`
- `edit_item/{itemId}`
- `detail/{itemId}`
- `settings`
- `expense_insights`

The Home add button opens `add_item`. Tapping an item opens its detail page, where Edit opens `edit_item/{itemId}`. Saving an item, deleting an item, or archiving an item returns to the previous screen.

## Development notes

The current MVP deliberately excludes barcode scanning, OCR, image uploads, payments, shared households, Cloud Functions, and AI features. Notifications remain the next substantial feature: create a notification channel, request Android 13+ notification permission, and schedule a daily WorkManager job that evaluates each active item's reminder window.
