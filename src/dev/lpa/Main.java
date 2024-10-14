package dev.lpa;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Main {

  public static void main(String[] args) {

    Path startingPath = Path.of(".."); // working directory ., .. is parent dir
    FileVisitor<Path> statsVisitor = new StatsVisitor(Integer.MAX_VALUE);
    try {
      Files.walkFileTree(startingPath, statsVisitor); // resource closed as part of execution
      // often anonymous class passed
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static class StatsVisitor implements FileVisitor<Path> {

    private Path initialPath = null;
    private final Map<Path, Map<String, Long>> folderSizes = new LinkedHashMap<>();
    private int initialCount;

    private int printLevel;

    private static final String DIR_CNT = "DirCount";
    private static final String FILE_SIZE = "fileSize";
    private static final String FILE_CNT = "fileCount";

    public StatsVisitor(int printLevel) {
      this.printLevel = printLevel;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(file);   // throws NullPointerException if arg == null
      Objects.requireNonNull(attrs);

//      folderSizes.merge(initialPath, Files.size(file), Long::sum);
//      folderSizes.merge(file.getParent(), 0L, (o, n) -> o += attrs.size());

      var parentMap = folderSizes.get(file.getParent());
      if (parentMap != null) {
        long fileSize = attrs.size();
        parentMap.merge(FILE_SIZE, fileSize, (o, n) -> o += n);
        parentMap.merge(FILE_CNT, 1L, Math::addExact);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

      Objects.requireNonNull(file);
      if (exc != null) {
        System.out.println(exc.getClass().getSimpleName() + " " + file);
      }
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
        folderSizes.put(dir, new HashMap<>());
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
          if (level < printLevel) {
            System.out.printf("%s[%s] - %,d bytes %n",
              "\t".repeat(level), key.getFileName(), value);
          }
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
