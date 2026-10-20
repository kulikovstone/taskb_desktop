# UI text

Use English for new user-facing labels and messages in the application.

# Feature screens

Place each feature screen in `screen/<screen_name>/` together with its Store, state, intents,
reducer, and other screen-specific classes. Do not create feature-level `mvi` folders.
Mirror this structure in screen-specific tests. Keep code shared by several screens outside
the individual screen folders.

# Screen MVI files

Keep the MVI declarations for every screen in separate files within its screen folder:

- `<Screen>State.kt` contains the screen state, its derived properties, and state-related
  models such as dialog types. Keep dialog models in the state file.
- `<Screen>Intent.kt` contains the screen's Intent type and its variants.
- `<Screen>Reducer.kt` contains the screen's Message type and its variants together with
  the `reduce<Screen>` function.
- `<Screen>Store.kt` contains the Store implementation, without declaring State, Intent,
  Message, or the reducer in the same file.
