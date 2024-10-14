package dev.lpa;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

    private int level;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(file);   // throws NullPointerException if arg == null
      Objects.requireNonNull(attrs);
      System.out.println("\t".repeat(level) + file.getFileName());
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

      Objects.requireNonNull(dir);
      Objects.requireNonNull(attrs);
      System.out.println("\t".repeat(level++) + dir.getFileName());
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

      Objects.requireNonNull(dir);
//      if (exc != null) {
//        throw exc;
//      }
      level--;
      return FileVisitResult.CONTINUE;
    }
  }
}
