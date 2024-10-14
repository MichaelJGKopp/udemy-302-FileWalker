package dev.lpa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class ChallengeStreams {

  public static void main(String[] args) {

    Path startingPath = Path.of("..");
    int index = startingPath.getNameCount();
    try (var paths = Files.walk(startingPath, Integer.MAX_VALUE)) {
      paths
        .filter(Files::isRegularFile)
        .collect(Collectors.groupingBy(p -> p.subpath(index, index + 1),
          Collectors.summarizingLong(
            (p -> {
              try {
                return Files.size(p);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }))))
        .entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .forEach(entry -> System.out.printf("[%s] %,d bytes, %d files %n",
              entry.getKey(), entry.getValue().getSum(), entry.getValue().getCount()
          ));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
