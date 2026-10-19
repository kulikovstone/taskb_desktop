# UI text

Use English for new user-facing labels and messages in the application.

# Feature screens

Place each feature screen in `screen/<screen_name>/` together with its Store, state, intents,
reducer, and other screen-specific classes. Do not create feature-level `mvi` folders.
Mirror this structure in screen-specific tests. Keep code shared by several screens outside
the individual screen folders.
