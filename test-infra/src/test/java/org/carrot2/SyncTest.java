package org.carrot2;

import com.carrotsearch.randomizedtesting.LifecycleScope;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyncTest extends TestBase {
  @Test
  public void testSanity() throws IOException {
    Path src = newTempDir(LifecycleScope.TEST);
    Path dst = newTempDir(LifecycleScope.TEST);

    Files.createFile(src.resolve("srcOnly.file"));
    Files.write(src.resolve("clash.file"), "Hello.".getBytes(StandardCharsets.UTF_8));
    Files.createDirectory(src.resolve("src.dir"));

    Files.createFile(dst.resolve("dstOnly.file"));
    Files.createFile(dst.resolve("clash.file"));
    Files.createDirectory(dst.resolve("dst.dir"));

    new Sync().sync(src, dst);

    Assertions.assertThat(listAll(src)).containsOnlyElementsOf(listAll(dst));
  }

  @Test
  public void testSubfolder() throws IOException {
    Path src1 = newTempDir(LifecycleScope.TEST);
    Path src2 = newTempDir(LifecycleScope.TEST);
    Path dst = newTempDir(LifecycleScope.TEST);

    Path sub = src1.resolve("subfolder2").resolve("deep");
    Files.createDirectories(sub);
    Files.createFile(sub.resolve("src.file"));

    new Sync().sync(src1, dst);
    Assertions.assertThat(listAll(src1)).containsOnlyElementsOf(listAll(dst));

    new Sync().sync(src2, dst);
    Assertions.assertThat(listAll(src2)).containsOnlyElementsOf(listAll(dst));
  }

  private List<String> listAll(Path src) throws IOException {
    try (Stream<Path> walk = Files.walk(src)) {
      return walk
          .filter(p -> !p.equals(src))
          .map(p -> {
            try {
              if (Files.isDirectory(p)) {
                return p.normalize().relativize(src).toString() + " [dir]";
              } else {
                String perms = "";
                PosixFileAttributeView posix = Files.getFileAttributeView(p, PosixFileAttributeView.class);
                if (posix != null) {
                  perms = posix.readAttributes().permissions().stream().map(Enum::name)
                      .collect(Collectors.joining(", "));
                }

                return String.format(Locale.ROOT,
                    "%s [file, %,d bytes, last modified: %s, perms: %s]",
                    p.normalize().relativize(src).toString(),
                    Files.size(p),
                    Files.getLastModifiedTime(p),
                    perms);
              }
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          })
          .collect(Collectors.toList());
    }
  }
}