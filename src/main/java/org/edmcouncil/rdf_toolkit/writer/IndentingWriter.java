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
package org.edmcouncil.rdf_toolkit.writer;

import java.io.IOException;
import java.io.Writer;

public class IndentingWriter extends Writer {

  protected final Writer out;
  protected int indentationLevel = 0;
  private String indentationString = "\t";
  private boolean indentationWritten = false;
  private String lineEnd;

  public IndentingWriter(Writer out) {
    this.out = out;
  }

  public void setIndentationString(String indentString) {
    this.indentationString = indentString;
  }

  public String getIndentationString() {
    return this.indentationString;
  }

  public int getIndentationLevel() {
    return this.indentationLevel;
  }

  public void setIndentationLevel(int indentationLevel) {
    this.indentationLevel = indentationLevel;
  }

  public void increaseIndentation() {
    ++this.indentationLevel;
  }

  public void decreaseIndentation() {
    --this.indentationLevel;
  }

  public String getLineEnd() {
    return this.lineEnd;
  }

  public void setLineEnd(String lineEnd) {
    this.lineEnd = lineEnd;
  }

  public void writeEOL() throws IOException {
    this.write(getLineEnd());
    this.indentationWritten = false;
  }

  public void close() throws IOException {
    this.out.close();
  }

  public void flush() throws IOException {
    this.out.flush();
  }

  public void write(char[] charBuffer, int off, int len) throws IOException {
    if (!this.indentationWritten) {
      for(var i = 0; i < this.indentationLevel; ++i) {
        this.out.write(this.indentationString);
      }

      this.indentationWritten = true;
    }

    this.out.write(charBuffer, off, len);
  }
}
