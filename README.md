# Contacts (OnePlus style)

An original, open-source Android contacts + dialer app whose look and feel is inspired by the
OnePlus Contacts app: white (or dark) background, red accent, top tabs, A-Z index, contact detail
screen and a clean dialpad. This is our own implementation, not a copy of OnePlus' proprietary APK.

## What is inside

- Contacts / Favorites / Recents tabs with a red underline indicator
- Alphabet section headers and a tap-able A-Z index on the right edge
- Contact detail screen: big avatar, Call / Message / Share actions, all phone numbers and emails
- Search by name or number
- T9 dialpad: type a number, matching contacts appear instantly
- Call history with All / Missed / Incoming / Outgoing filters, calls from one number grouped
- Favorites (starred contacts)
- Local spam blocking (on-device blocklist, calls rejected by CallScreeningService)
- Dual SIM selection when two SIMs are active
- Light and dark theme (follows the system setting)
- Works fully offline

## Install

Download the APK from the latest release and install it. Android 7.0 (API 24) or newer.

For automatic spam rejection, set the app as the call screening app in Android settings
(Phone app -> Settings -> Caller ID & spam -> Call screening app).

## Build

The APK is built by GitHub Actions on every push to `main` (`.github/workflows/build-apk.yml`).
Locally:

```
gradle assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.
