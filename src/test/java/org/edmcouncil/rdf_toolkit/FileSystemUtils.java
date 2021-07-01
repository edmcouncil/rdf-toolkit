/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.edmcouncil.rdf_toolkit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FileSystemUtils {

  public static final String ONTOLOGIES_DIR = "ontologies";
  public static final File RESOURCE_DIR = new File("src/test/resources");

  public static File constructTargetPath(File sourceFile, File fromDir, File toDir) {
    var targetFile = new File(
        replaceDirStem(sourceFile.getParentFile(), fromDir, toDir),
        sourceFile.getName());
    return makeDir(targetFile);
  }

  public static File constructTargetPath(File sourceFile, File fromDir, File toDir, String newStem) {
    var targetFile = new File(
        replaceDirStem(sourceFile.getParentFile(), fromDir, toDir),
        setFilePathExtension(sourceFile.getName(), newStem));
    return makeDir(targetFile);
  }

  public static File createTempDir(File parent, String prefix) throws IOException {
    return Files.createTempDirectory(parent.toPath(), prefix).toFile();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static File createDir(File parentDir, String name) {
    var newDir = new File(parentDir, name);
    newDir.mkdirs();
    return newDir;
  }

  public static List<File> listDirTreeFiles(File dir) {
    var result = new ArrayList<File>();

    if (dir.exists()) {
      if (dir.isDirectory()) {
        for (var file : dir.listFiles()) {
          result.addAll(listDirTreeFiles(file));
        }
      } else {
        var isExcluded = listDirTreeFilesExcludeRegexes()
            .stream()
            .anyMatch(regex -> Pattern.compile(regex).matcher(dir.getName()).find());
        if (!isExcluded) {
          result.add(dir); // 'dir' is actually a file
        }
      }
    }

    return result;
  }

  /**
   * Creates a clean directory at the given path.  Deletes any existing contents.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static File mkCleanDir(String dirPath) {
    var newDir = new File(dirPath);
    if (newDir.exists()) {
      deleteFile(newDir);
    }
    newDir.mkdirs();
    return newDir;
  }

  /**
   * Deletes the given file or directory.  For a directory, deletes recursively.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void deleteFile(File file) {
    if (file.isDirectory()) {
      for (File childFile : file.listFiles()) {
        deleteFile(childFile);
      }
    }
    file.delete();
  }

  public static String getFileContents(File file, String encoding) throws IOException {
    return Files.readString(file.toPath(), Charset.forName(encoding));
  }

  public static List<String> readFileLines(File file, String encoding) throws IOException {
    return Files.readAllLines(file.toPath(), Charset.forName(encoding));
  }

  public static Set<String> listDirTreeFilesExcludeRegexes() {
    return Set.of("\\.conf", "^About", "catalog\\d+\\.xml");
  }

  public static File getRawRdfDirectory() {
    var rawRdfDirectory = new File(RESOURCE_DIR.getPath() + "/" + ONTOLOGIES_DIR);
    assertTrue(rawRdfDirectory.isDirectory(), String.format("%s is not a directory", rawRdfDirectory));
    assertTrue(rawRdfDirectory.exists(), String.format("%s does not exists", rawRdfDirectory));
    return rawRdfDirectory;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static File makeDir(File targetFile) {
    if (!targetFile.getParentFile().exists()) {
      targetFile.getParentFile().mkdirs();
    }
    return targetFile;
  }

  private static File replaceDirStem(File file, File fromDir, File toDir) {
    try {
      var filePath = file.getCanonicalPath();
      var fromDirPath = fromDir.getCanonicalPath();
      var toDirPath = toDir.getCanonicalPath();
      if (filePath.startsWith(fromDirPath)) {
        return new File(toDirPath + filePath.substring(fromDirPath.length()));
      } else {
        return file;
      }
    } catch (IOException ex) {
      fail(ex.getClass().getSimpleName() + " occurred while replacing dir stem. Details: " + ex.getMessage());
      return null;
    }
  }

  private static String setFilePathExtension(String filePath, String fileExtension) {
    if (filePath.contains(".")) {
      return filePath.substring(0, filePath.lastIndexOf(".")) + fileExtension;
    } else {
      return filePath + fileExtension;
    }
  }
}
