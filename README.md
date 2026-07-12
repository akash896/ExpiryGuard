# ExpiryGuard

ExpiryGuard is an Android app for tracking the expiry dates, value, and lifecycle of household items. It is designed for food, medicines, documents, warranties, cosmetics, subscriptions, and other items that need attention before they expire.

## Current features

- Firebase Email/Password Authentication protects each user's items after onboarding.
- A three-page onboarding flow introduces expiry tracking, value insights, and notification reminders on first launch, followed by Login / Sign up.
- Cloud Firestore stores each user's data at `users/{userId}/items/{itemId}`.
- Home groups active items by expiry urgency and provides search, category filters, and value summaries. Its bottom-end add button leaves every list item fully scrollable and readable.
- Home includes Quick Add templates for common food, medicine, document, warranty, and subscription items.
- Calendar View shows upcoming expiries by month, highlights dates with expiring items, and opens item details from a selected date.
- Shopping List keeps manual and rebuy items, with checked items separated from items still to buy.
- Add/Edit supports name, category, expiry date, optional purchase date, quantity, price, currency, reminder period, and notes.
- Expense Insights supports daily, monthly, quarterly, and annual navigation with waste percentage, category leaders, sortable breakdowns, and spending bars.
- Item Detail shows all saved fields, expiry timing, created/updated timestamps, value state, and lifecycle actions.
- An item can be marked consumed, restored to not consumed, archived, or deleted. Archiving and deleting require confirmation.
- Navigation uses explicit routes for Home, Add Item, Edit Item, Item Detail, Settings, and Expense Insights. Screens other than Home provide a back action in the top app bar.
- Settings provides a preferred daily reminder-check time and category-specific default reminder periods.
- Each item has a Notify switch with its label below the toggle, and the notification itself has a Stop action that disables reminders for that item.
- Expired items are shown under the dynamic Expired category without losing their original category for expense history. Expired can also be chosen when adding an item.
- Settings shows account and notification status, supports a manual reminder check, archives items expired more than 30 days after confirmation, and stores the selected light or dark theme locally.
- The launcher icon uses `ExpiryGuard_Icon_v2.png` with density-specific standard and round Android resources.

## Technology

- Kotlin and Jetpack Compose with Material 3
- MVVM and a repository abstraction
- Navigation Compose
- Firebase Authentication and Cloud Firestore, managed through the Firebase BoM
- Kotlin coroutines and `Flow`
- WorkManager runs the daily Firebase-backed reminder check
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
2. In Firebase Authentication, enable the **Email/Password** sign-in provider. Anonymous Authentication is no longer used by the app.
3. Create a Cloud Firestore database.
4. Apply rules that allow an authenticated user to access only their own profile document and subcollections:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /users/{userId}/items/{itemId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /users/{userId}/shoppingList/{shoppingItemId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

Shopping List entries are stored separately at `users/{userId}/shoppingList/{shoppingItemId}`.

Usernames are converted internally to an app-only Firebase email address. Firebase Authentication securely stores and validates credentials; the app never writes a password to Firestore or local storage. It also attempts to save the display username and timestamps at `users/{userId}` when the deployed Firestore rules allow that profile document. Item access does not depend on this optional profile write.

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

## First Launch and Account

On first launch, ExpiryGuard shows three onboarding pages followed by a Login / Sign up screen. Use Next and Back to move through onboarding, or Skip to continue directly to account setup. The final onboarding page offers Android 13+ notification permission; declining it does not block account creation or app access.

For a first local test, select **Sign up** and use username `akash896` with password `Abcd.1234`. To exercise Login afterward, clear the app's local storage or reinstall it, complete/skip onboarding, then choose **Login** with the same credentials. A username must be 3-30 lowercase letters, numbers, dots, underscores, or hyphens; passwords must contain at least 8 characters. Authentication failures never open Home.

## Project layout

```text
app/src/main/java/com/akash/expiryguard/
  data/model/          Item, category, status, and expense models
  data/auth/           Firebase Email/Password account access
  data/repository/     Repository contract
  data/firebase/       Firebase Auth and Firestore implementations
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
- `calendar`
- `shopping_list`
- `onboarding`
- `authentication`

The Home add button opens `add_item`. Tapping an item opens its detail page, where Edit opens `edit_item/{itemId}`. Saving an item, deleting an item, or archiving an item returns to the previous screen.

## Quick Add

Quick Add templates on Home open the regular Add Item form with a name, category, reminder period, and today's purchase date prefilled. Expiry date remains empty and must be reviewed before saving. Templates do not save an item automatically.

## Shopping List

Open Shopping List from Home to add a manual item, edit its name/category/quantity/estimated price, mark it checked, or delete it. Item Detail also offers Add to shopping list, which copies the item’s rebuy details without changing the original expiry item.

## Calendar View

Open Calendar from Home to browse a month of expiry dates. Every date with one or more expiring items has an indicator; expired dates use a danger indicator and today is highlighted. Select a date to view its expiring items, then tap an item to open its detail screen.

## Notifications

ExpiryGuard creates an `expiry_reminders` notification channel on Android 8.0+. On Android 13+, the final onboarding page explains the benefit and offers the `POST_NOTIFICATIONS` permission request. If permission is not granted, an item Notify switch remains off; enabling it requests permission first and only saves the enabled state after permission is available. If notifications are disabled in Android system settings, the app leaves the item switch off and asks the user to enable notifications there. Settings stores a preferred daily check time locally, defaulting to 09:00, and WorkManager schedules the Firebase-backed check near that time. Android can defer background work, so this is a best-effort schedule rather than an exact alarm.

An item is eligible from the configured reminder day through its expiry day, and receives at most one reminder per day. The notification includes a Stop action that disables the item's `notificationsEnabled` flag in Firestore, records a local stop marker for offline reliability, and removes the current alert. The Notify switch on a Home card or Add/Edit screen can enable reminders again. Notification text includes the category, expiry date, and price as a possible expired value when available.

After device boot or a system time change, ExpiryGuard reschedules the daily WorkManager job and queues one check. Android can still defer background work when network access is unavailable.

## Expired Category

The Expired filter includes any item whose expiry date has passed, plus items deliberately saved with the `Expired` category. This is a dynamic display category: automatically expired items keep their original stored category so existing expense breakdowns remain meaningful.

## Settings

Settings identifies the signed-in username, notification access, Firebase storage, and local use of price data. It lets you select the preferred reminder-check time, set category reminder defaults for new items, run an expiry check, send a test notification, and archive active items whose expiry date is more than 30 days old after confirmation. The light/dark theme choice is saved locally and applies throughout the app.

## Development notes

The current MVP deliberately excludes barcode scanning, OCR, image uploads, payments, shared households, Cloud Functions, and AI features.
