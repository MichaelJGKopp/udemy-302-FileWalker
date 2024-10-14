package dev.lpa;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

  public static void main(String[] args) {

    Path startingPath = Path.of("."); // working directory
    FileVisitor<Path> statsVisitor = new StatsVisitor();
    try {
      Files.walkFileTree(startingPath, statsVisitor); // resource closed as part of execution
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static class StatsVisitor extends SimpleFileVisitor<Path> {

    private Path initialPath = null;
    private final Map<Path, Long> folderSizes = new LinkedHashMap<>();
    private int initialCount;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(file);   // throws NullPointerException if arg == null
      Objects.requireNonNull(attrs);

//      folderSizes.merge(initialPath, Files.size(file), Long::sum);
      folderSizes.merge(file.getParent(), 0L, (o, n) -> o += attrs.size());
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(dir);
      Objects.requireNonNull(attrs);

      if (initialPath == null) {
        initialPath = dir;
        initialCount = dir.getNameCount();
      } else {
        int relativeLevel = dir.getNameCount() - initialCount;
        if (relativeLevel == 1) {
          folderSizes.clear();
        }
        folderSizes.put(dir, 0L);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

      Objects.requireNonNull(dir);
//      if (exc != null) {
//        throw exc;
//      }

      if (dir.equals(initialPath)) {
        return FileVisitResult.TERMINATE;
      }

      int relativeLevel = dir.getNameCount() - initialCount;
      if (relativeLevel == 1) {
        folderSizes.forEach((key, value) -> {

          int level = key.getNameCount() - initialCount - 1;
          System.out.printf("%s[%s] - %,d bytes %n",
            "\t".repeat(level), key.getFileName(), value);
        });
      } else {
        long folderSize = folderSizes.get(dir);
        folderSizes.merge(dir.getParent(), 0L, (o, n) -> o += folderSize);
//        folderSizes.merge(dir.getParent(), folderSize, Long::sum);
      }

      return FileVisitResult.CONTINUE;
    }
  }
}
