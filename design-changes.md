# Design changes (A2)

1) Switched runnable app entry point to a text UI (ConsoleApp)
- Why: The previous JAR only contained a manifest and did not include compiled classes; also JavaFX apps often fail to run on machines without JavaFX modules.
- Unavoidable: We needed a reliably runnable JAR for grading.

2) Removed JavaFX dependency from `User`
- Why: `User` used ObservableList/FXCollections, which prevents compilation/running without JavaFX.
- Unavoidable: The runnable version must not depend on JavaFX.

3) Added `Realm` (fixed offset) and updated `QuestEvent` to include a Realm
- Why: Realms + local time conversion are part of the core requirements.
- Unavoidable: Required by the spec for A2 coverage.
