package com.mkulikov.buildlogic;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GitVersionSourceTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    private record Repository(Path directory) {
        String git(String... arguments) throws IOException, InterruptedException {
            var command = new ArrayList<>(List.of("git", "-c", "commit.gpgsign=false", "-c",
                    "core.hooksPath=" + directory.resolve("disabled-hooks")));
            command.addAll(List.of(arguments));
            var builder = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true);
            builder.environment().putAll(java.util.Map.of(
                    "GIT_AUTHOR_NAME", "Version test", "GIT_AUTHOR_EMAIL", "version@example.com",
                    "GIT_COMMITTER_NAME", "Version test", "GIT_COMMITTER_EMAIL", "version@example.com"));
            Process process = builder.start();
            String output;
            try (var stream = process.getInputStream()) {
                output = new String(stream.readAllBytes(), StandardCharsets.UTF_8).strip();
            }
            assertEquals(output, 0, process.waitFor());
            return output;
        }

        void write(String name, String content) throws IOException {
            Files.writeString(directory.resolve(name), content);
        }

        void commit() throws IOException, InterruptedException {
            git("add", "--all");
            git("commit", "--quiet", "-m", "Test commit");
        }

        String version() {
            return GitVersionSource.calculate(directory);
        }
    }

    private Repository repository() throws Exception {
        var repository = new Repository(temporary.newFolder("repository with spaces").toPath());
        repository.git("init", "--quiet", "--initial-branch=master");
        repository.write("initial.txt", "Initial project");
        repository.commit();
        return repository;
    }

    @Test
    public void configuredBaseIncrementsAndRebuildingDoesNotChangeHistory() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1.16\n");
        assertEquals("1.16", repository.version());
        repository.commit();
        assertEquals("1.16", repository.version());
        String firstRevision = null;
        for (int revision = 1; revision <= 10; revision++) {
            repository.write("initial.txt", "Change " + revision);
            repository.commit();
            assertEquals("1." + (16 + revision), repository.version());
            if (revision == 1) firstRevision = repository.git("rev-parse", "HEAD");
        }
        String head = repository.git("rev-parse", "HEAD");
        assertEquals("1.26", repository.version());
        assertEquals("1.26", repository.version());
        assertEquals(head, repository.git("rev-parse", "HEAD"));
        assertEquals("", repository.git("status", "--porcelain"));
        repository.git("checkout", "--quiet", "--detach", firstRevision);
        assertEquals("1.17", repository.version());
    }

    @Test
    public void changingEitherBaseComponentStartsANewSeriesIncludingWhenReusingABase() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1.16\n");
        repository.commit();
        repository.write("initial.txt", "Change");
        repository.commit();
        assertEquals("1.17", repository.version());
        repository.write("version-major.txt", "1.30\n");
        assertEquals("1.30", repository.version());
        repository.commit();
        assertEquals("1.30", repository.version());
        repository.write("initial.txt", "Change after updating the second component");
        repository.commit();
        assertEquals("1.31", repository.version());
        repository.write("version-major.txt", "2.0\n");
        assertEquals("2.0", repository.version());
        repository.commit();
        assertEquals("2.0", repository.version());
        repository.write("initial.txt", "Another change");
        repository.commit();
        assertEquals("2.1", repository.version());
        repository.write("version-major.txt", "1.16\n");
        repository.commit();
        assertEquals("1.16", repository.version());
    }

    @Test
    public void whitespaceOnlyChangesCountAsOrdinaryCommits() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1.16\n");
        repository.commit();
        repository.write("initial.txt", "Change");
        repository.commit();
        repository.write("version-major.txt", "\n1.16\n\n");
        repository.commit();
        assertEquals("1.18", repository.version());
    }

    @Test
    public void mergeCountsOnceAndMergedBaseChangeResetsRevision() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1.16\n");
        repository.commit();
        repository.git("checkout", "--quiet", "-b", "feature");
        repository.write("feature.txt", "First change");
        repository.commit();
        repository.write("feature.txt", "Second change");
        repository.commit();
        repository.git("checkout", "--quiet", "master");
        repository.write("main.txt", "Main branch change");
        repository.commit();
        repository.git("merge", "--quiet", "--no-ff", "--no-edit", "feature");
        assertEquals("1.18", repository.version());
        repository.git("checkout", "--quiet", "-b", "next-major");
        repository.write("version-major.txt", "2.0\n");
        repository.commit();
        repository.git("checkout", "--quiet", "master");
        repository.git("merge", "--quiet", "--no-ff", "--no-edit", "next-major");
        assertEquals("2.0", repository.version());
    }

    @Test
    public void invalidBaseVersionIsRejected() throws Exception {
        var repository = repository();
        for (String baseVersion : List.of("", "1", "1.", ".12", "1.2.3", "1,12", "-1.16", "1.-12", "01.16", "1.012", "abc", "<script>")) {
            repository.write("version-major.txt", baseVersion);
            var error = assertThrows(IllegalArgumentException.class, repository::version);
            assertTrue(error.getMessage().contains("version-major.txt должен содержать"));
        }
    }

    @Test
    public void secondComponentIncrementsAsAnIntegerWithoutCarryingOrRounding() throws Exception {
        var repository = repository();
        for (var versions : List.of(List.of("0.0", "0.1"), List.of("1.9", "1.10"),
                List.of("1.99", "1.100"), List.of("7.9007199254740992", "7.9007199254740993"))) {
            String base = versions.get(0);
            repository.write("version-major.txt", base + "\n");
            repository.commit();
            assertEquals(base, repository.version());
            repository.write("initial.txt", "Change after " + base);
            repository.commit();
            assertEquals(versions.get(1), repository.version());
        }
    }

    @Test
    public void migratingFromOldIntegerFormatStartsAtTheConfiguredBase() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1\n");
        repository.commit();
        repository.write("initial.txt", "Change before migrating");
        repository.commit();
        repository.write("version-major.txt", "1.16\n");
        assertEquals("1.16", repository.version());
        repository.commit();
        assertEquals("1.16", repository.version());
        repository.write("initial.txt", "Change after migrating");
        repository.commit();
        assertEquals("1.17", repository.version());
    }

    @Test
    public void shallowCheckoutIsRejected() throws Exception {
        var repository = repository();
        repository.write("version-major.txt", "1.16\n");
        repository.commit();
        repository.write("initial.txt", "Change");
        repository.commit();
        Path shallow = temporary.newFolder("shallow").toPath();
        repository.git("clone", "--quiet", "--depth", "1", repository.directory().toUri().toString(), shallow.toString());
        var error = assertThrows(IllegalStateException.class, () -> GitVersionSource.calculate(shallow));
        assertTrue(error.getMessage().contains("нужна полная история Git"));
    }

    @Test
    public void missingGitHistoryIsRejected() throws Exception {
        Path directory = temporary.newFolder("archive").toPath();
        Files.writeString(directory.resolve("version-major.txt"), "1.16\n");
        var error = assertThrows(IllegalStateException.class, () -> GitVersionSource.calculate(directory));
        assertTrue(error.getMessage().contains("Git-репозиторий с полной историей"));
    }
}
