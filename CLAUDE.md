# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

SportIsrael is an Android app (Java, XML layouts) that helps people find sports courts and organize
sports groups in Israel. It uses Google Maps SDK for the court map and Firebase (Auth + Firestore) as
the backend — there is no custom server. See [README.md](README.md) for the project's origin story.

## Build & run

This is a legacy Android Gradle project (AGP 4.2.2, Gradle 6.7.1, `compileSdkVersion 27`), which requires
**JDK 11** — a newer default JDK will fail the build. Set `JAVA_HOME` before invoking Gradle if it isn't
already pointed at a JDK 11 install:

```powershell
$env:JAVA_HOME = "C:\Program Files\AdoptOpenJDK\jdk-11.0.11.9-hotspot"
.\gradlew.bat :app:assembleDebug
```

Other common tasks:
- Full build: `.\gradlew.bat assembleDebug --console=plain`
- Clean: `.\gradlew.bat clean`
- Lint: `.\gradlew.bat lint`
- Unit tests: `.\gradlew.bat test`
- Instrumented tests (require an emulator/device): `.\gradlew.bat connectedAndroidTest` — there is
  currently only one placeholder test, [ExampleInstrumentedTest.java](app/src/androidTest/java/com/example/ykrin/sportisrael/ExampleInstrumentedTest.java)
- Single test class: `.\gradlew.bat test --tests "com.example.ykrin.sportisrael.SomeTest"`

`app/google-services.json` is required for Firebase to initialize and is present in the repo (despite
being gitignored upstream) for this project. Google Maps requires a `google_maps_key` string resource.

## Data import script

[scripts/import_osm_courts.js](scripts/import_osm_courts.js) is a standalone Node script (not part of the
Gradle build) that pulls real courts from OpenStreetMap's Overpass API and writes them into the
Firestore `courts` collection. It needs its own `npm install` inside `scripts/` and a Firebase service
account key (`scripts/serviceAccountKey.json`, gitignored, never commit it). Defaults to a dry run;
pass `--write` to actually write to Firestore. See the file's header comment for full usage/options.

## Architecture

**No MVVM/MVP layer, no dependency injection, no repository abstraction.** Each `Activity` talks to
`FirebaseAuth`/`FirebaseFirestore` directly in `onCreate`/callbacks. When working in this codebase,
match that style rather than introducing new architectural layers.

### Navigation

Three bottom-tab screens — `GroupsActivity`, `MapActivity`, `ProfileActivity` — share one
[NavigationBar.java](app/src/main/java/com/example/ykrin/sportisrael/NavigationBar.java), which every
tab-bearing activity re-instantiates and wires to its own `BottomNavigationView` (`R.id.navigation_bar`
in each layout). `MainActivity` is the launcher/landing screen; `AuthActivity` is a single dual-mode
screen for both sign-in and registration (toggled via tabs), replacing separate login/register
activities from earlier in the project's history.

### Firebase data model (Firestore, no server-side rules/functions in this repo)

- **`users/{uid}`** — directory of display names/emails so features like invites can look people up.
  Maintained by [UserDirectory.java](app/src/main/java/com/example/ykrin/sportisrael/UserDirectory.java)
  (`ensureUserDocument` on every relevant screen load, `writeUserDocument` explicitly at registration).
  Has an `invites` subcollection per user (pending group invitations).
- **`courts/{title}`** — document ID is the court's title (see `MapActivity.on_create_court_button_click`
  and `CourtInformationActivity`), not an auto-generated ID. [Court.java](app/src/main/java/com/example/ykrin/sportisrael/Court.java)
  models title/description/location(`GeoPoint`)/state/sport/source/osmId. `state` is one of
  [CourtState.java](app/src/main/java/com/example/ykrin/sportisrael/CourtState.java) (`empty`/`full`/`searching`/`unknown`),
  `sport` is one of [SportType.java](app/src/main/java/com/example/ykrin/sportisrael/SportType.java). OSM-imported
  courts encode structured facts (surface, lighting, access) as `"key: value"` segments joined by `·`
  inside `description`; `CourtInformationActivity.showParsedDetails` parses that back out for display.
- **`groups/{id}`** — modeled by [Group.java](app/src/main/java/com/example/ykrin/sportisrael/Group.java).
  Membership/admin sets are stored as `Map<uid, true>` rather than arrays — deliberately, because the
  bundled Firestore SDK (15.0.0) predates `arrayUnion`/`whereArrayContains`; membership maps are instead
  queried with `whereEqualTo(FieldPath.of("memberIds", uid), true)`. For the same SDK-version reason,
  `memberCount` is a denormalized field kept in sync by hand (no `FieldValue.increment`) inside
  transactions in [GroupActions.java](app/src/main/java/com/example/ykrin/sportisrael/GroupActions.java),
  which owns every membership mutation (`join`, `leave`, `requestJoin`, `approveRequest`, invites) so
  that map fields and `memberCount` never drift apart. Has `joinRequests` and (implicitly, via
  `users/{uid}/invites`) invite subcollections. Keep new membership-mutating code going through
  `GroupActions` rather than writing to `groups` documents directly.

### Maps

`MapActivity` renders court markers via
[MarkerIconGenerator.java](app/src/main/java/com/example/ykrin/sportisrael/MarkerIconGenerator.java),
which draws a cached circular bitmap (sport-colored background + white sport icon) per `SportType`/color
combo — extend the cache key if a new visual variant (e.g. occupancy-based color) is added. Adding a new
court is a two-step flow: create at the device's current location, then optionally drag the marker and
call `set_final_court_location` to persist the adjusted `GeoPoint`.

### Enums as lookup tables

`SportType`, `CourtState`, and `SkillLevel` all follow the same pattern: a Firestore-stored string
`value`, a human-readable `displayName`, and a static `fromValue(String)` that falls back to a safe
default (`OTHER`/`UNKNOWN`/`ANY`) instead of throwing. Follow this pattern for any new enum-like field
stored in Firestore.

### Debug-only quick login

`AuthActivity` shows a "dev quick login" section gated by `BuildConfig.DEBUG`, signing straight into one
of three fixed test accounts (`admin@sportisrael.test`, `user@sportisrael.test`,
`organizer@sportisrael.test`, all password `test123456`). It must never be reachable in a release build.
