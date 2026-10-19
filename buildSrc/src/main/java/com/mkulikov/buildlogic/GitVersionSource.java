package com.mkulikov.buildlogic;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Adds commits since the configured base version to its last component. */
public abstract class GitVersionSource implements ValueSource<String, GitVersionSource.Parameters> {
    private static final String VERSION_FILE = "version-major.txt";

    public interface Parameters extends ValueSourceParameters {
        DirectoryProperty getRepositoryDirectory();
    }

    @Override
    public String obtain() {
        return calculate(getParameters().getRepositoryDirectory().get().getAsFile().toPath());
    }

    public static String calculate(Path directory) {
        final String baseVersion;
        try {
            baseVersion = Files.readString(directory.resolve(VERSION_FILE)).strip();
        } catch (IOException cause) {
            throw new IllegalStateException("Не удалось прочитать " + VERSION_FILE, cause);
        }
        if (!baseVersion.matches("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)")) {
            throw new IllegalArgumentException(VERSION_FILE + " должен содержать версию из двух целых неотрицательных чисел через точку, например 1.16 или 2.0.");
        }
        int separator = baseVersion.indexOf('.');
        String major = baseVersion.substring(0, separator);
        BigInteger initialRevision = new BigInteger(baseVersion.substring(separator + 1));
        if (git(directory, "rev-parse", "--is-shallow-repository").equals("true")) {
            throw new IllegalStateException("Для вычисления версии нужна полная история Git. Выполните git fetch --unshallow; в CI используйте fetch-depth: 0.");
        }

        String baseline = null;
        String changes = git(directory, "log", "--first-parent", "--format=%H", "HEAD", "--", VERSION_FILE);
        for (String commit : changes.lines().toList()) {
            if (commit.isBlank()) continue;
            // A deleted file or a different base version ends the current version series.
            if (git(directory, "ls-tree", commit, "--", VERSION_FILE).isEmpty()) break;
            if (!git(directory, "show", commit + ":" + VERSION_FILE).equals(baseVersion)) break;
            // Whitespace-only edits do not reset the counter.
            baseline = commit;
        }
        // Uncommitted base versions have no additional commits yet.
        String revision = baseline == null ? "0"
                : git(directory, "rev-list", "--first-parent", "--count", baseline + "..HEAD");
        return major + "." + initialRevision.add(new BigInteger(revision));
    }

    private static String git(Path directory, String... arguments) {
        var command = new ArrayList<>(List.of("git", "-c", "log.showSignature=false", "-c", "color.ui=false"));
        command.addAll(List.of(arguments));
        try {
            Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
            final String output;
            try (var stream = process.getInputStream()) {
                output = new String(stream.readAllBytes(), StandardCharsets.UTF_8).strip();
            }
            if (process.waitFor() != 0) {
                throw new IllegalStateException("Не удалось вычислить версию приложения. Нужен Git-репозиторий с полной историей коммитов. " + output);
            }
            return output;
        } catch (IOException cause) {
            throw new IllegalStateException("Не удалось вычислить версию приложения. Нужен установленный Git и полная история репозитория.", cause);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Вычисление версии приложения прервано.", cause);
        }
    }
}
