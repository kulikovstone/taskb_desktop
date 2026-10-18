# Xerial loads its driver via JDBC and calls SQLite classes through JNI.
-keep class org.sqlite.** { *; }

# These implementations are instantiated through META-INF/services.
-keep class org.jetbrains.exposed.v1.jdbc.ExposedConnectionImpl {
    public <init>();
}
-keep class org.slf4j.simple.SimpleServiceProvider {
    public <init>();
}
