package dev.lpa;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Main {

  public static void main(String[] args) {

    Path startingPath = Path.of("."); // working directory ., .. is parent dir
    FileVisitor<Path> statsVisitor = new StatsVisitor(Integer.MAX_VALUE);
    try {
      Files.walkFileTree(startingPath, statsVisitor); // resource closed as part of execution
      // often anonymous class passed
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static class StatsVisitor extends SimpleFileVisitor<Path> {

    private Path initialPath = null;
    private final Map<Path, Map<Info, Long>> folderSizes = new LinkedHashMap<>();
    private int initialCount;

    private int printLevel;

    public StatsVisitor(int printLevel) {
      this.printLevel = printLevel;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(file);   // throws NullPointerException if arg == null
      Objects.requireNonNull(attrs);

      folderSizes.merge(file.getParent(), Info.getEmptyMap(), (o, n) -> {
        Long[] infoAr = {attrs.size(), 1L, 0L};
        int[] j = {0};
        for (var i : Info.values()) {
          o.merge(i, 0L, (o2, n2) -> o2 += infoAr[j[0]++]);
        }
        return o;
      });
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
        folderSizes.put(dir, Info.getEmptyMap());
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

      Objects.requireNonNull(dir);

      if (dir.equals(initialPath)) {
        return FileVisitResult.TERMINATE;
      }

      int relativeLevel = dir.getNameCount() - initialCount;
      if (relativeLevel == 1) {
        folderSizes.forEach((key, value) -> {
          int level = key.getNameCount() - initialCount - 1;
          if (level < printLevel) {
            System.out.printf("%s[%s] - %,d bytes\tFiles %d\tFolders %d %n",
              "\t".repeat(level), key.getFileName(),
              value.get(Info.SIZE), value.get(Info.FILES), value.get(Info.FOLDERS));
          }
        });
      } else {
        var infoMap = folderSizes.get(dir);
        folderSizes.merge(dir.getParent(), Info.getEmptyMap(), (o, n) -> {
          for (var i : Info.values()) {
            o.merge(i, 0L, (o2, n2) -> i == Info.FOLDERS ? (o2 + 1) : o2 + infoMap.get(i));
          }
          return o;
        });
      }

      return FileVisitResult.CONTINUE;
    }
  }
}