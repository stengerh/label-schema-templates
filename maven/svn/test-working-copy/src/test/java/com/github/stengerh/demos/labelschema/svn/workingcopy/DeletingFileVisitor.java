package com.github.stengerh.demos.labelschema.svn.workingcopy;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;

public class DeletingFileVisitor extends SimpleFileVisitor<Path> {
  private final boolean force;

  public DeletingFileVisitor(boolean force) {
    this.force = force;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (force) {
      clearReadOnlyAttribute(file);
    }

    Files.delete(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    Files.delete(dir);
    return FileVisitResult.CONTINUE;
  }

  private void clearReadOnlyAttribute(Path file) throws IOException {
    DosFileAttributeView dosAttributes = Files.getFileAttributeView(file, DosFileAttributeView.class);
    if (dosAttributes != null) {
      dosAttributes.setReadOnly(false);
    }
  }
}
