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

package org.edmcouncil.rdf_toolkit.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for iterating over directory trees.
 */
public class DirectoryWalker {

  private final File rootDir;
  private final Pattern relativePathPattern;

  static public class DirectoryWalkerResult {
    private final File file;
    private final String relativePath;

    public DirectoryWalkerResult(File file, String relativePath) {
      this.file = file;
      this.relativePath = relativePath;
    }

    public File getFile() {
      return file;
    }

    public String getRelativePath() {
      return relativePath;
    }
  }

  /**
   * Constructor that takes a root directory and a relative file path pattern.
   */
  public DirectoryWalker(File rootDir, Pattern relativePathPattern) {
    this.rootDir = rootDir;
    this.relativePathPattern = relativePathPattern;
    if (!rootDir.exists()) {
      throw new IllegalArgumentException(String.format("directory does not exist: %s", rootDir.getAbsolutePath()));
    }
    if (!rootDir.isDirectory()) {
      throw new IllegalArgumentException(String.format("file is not a directory: %s", rootDir.getAbsolutePath()));
    }
    if (!rootDir.canRead()) {
      throw new IllegalArgumentException(String.format("directory cannot be read: %s", rootDir.getAbsolutePath()));
    }
  }

  /**
   * Returns all file matches for the directory walker.
   */
  public Collection<DirectoryWalkerResult> pathMatches() {
    return pathMatches(rootDir);
  }

  /**
   * Returns all file matches for the directory walker, starting at a particular directory.
   */
  private Collection<DirectoryWalkerResult> pathMatches(File startDir) {
    if (startDir == null) {
      throw new IllegalStateException("startDir cannot be bull.");
    }

    List<DirectoryWalkerResult> matches = new ArrayList<>();

    var rootPath = rootDir.toPath();
    var files = startDir.listFiles();
    if (files != null) {
      for (File child : files) {
        if (child.isDirectory()) {
          matches.addAll(pathMatches(child));
        } else {
          var childPath = child.toPath();
          var relativePath = rootPath.relativize(childPath).toString();
          var relativePathMatcher = relativePathPattern.matcher(relativePath);
          if (relativePathMatcher.matches()) {
            matches.add(new DirectoryWalkerResult(child, relativePath));
          }
        }
      }
    }
    return matches;
  }
}
