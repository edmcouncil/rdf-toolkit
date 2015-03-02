/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 *
 * The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package org.edmcouncil.rdf_serializer

import org.edmcouncil.{SerializerApiSesame, SerializerApiOWLAPI}

import scala.collection.mutable

object Serializer {

  //
  // For now, just do it quick & dirty by recreating a command line for the SesameRdfFormatter so that we
  // don't have to touch Tony's code. Eventually we should create a SesameSerializer class next to the
  // OwlApiSerializer.
  //
  private def runSesameRdfFormatter(params: CommandLineParams2): Int = {

    //
    // usage: SesameRdfFormatter
    // -h,--help                     print out details of the command-line
    //                               arguments for the program
    // -s,--source <arg>             source (input) RDF file to be formatting
    // -sfmt,--source-format <arg>   source (input) RDF format; one of: auto
    //                               (select by filename) [default], binary,
    //                               json-ld (JSON-LD), n3, n-quads (N-quads),
    //                               n-triples (N-triples), rdf-a (RDF/A),
    //                               rdf-json (RDF/JSON), rdf-xml (RDF/XML),
    //                               trig (TriG), trix (TriX), turtle (Turtle)
    // -t,--target <arg>             target (output) RDF file
    // -tfmt,--target-format <arg>   source (input) RDF format: one of: turtle
    //                               (Turtle, sorted) [default]
    //
    def sesameRdfFormatterArgs: Array[String] = {
      val ab = mutable.ArrayBuilder.make[String]

      if (params.inputFiles.hasValue)
        ab ++= Seq("-s", params.inputFiles.value.head.fileName.get)

      ab ++= Seq("-sfmt", "auto")

      if (params.outputFile.hasValue)
        ab ++= Seq("-t", params.outputFile.value.get.fileName.get)

      if (params.outputFormat.hasValue)
        ab ++= Seq("-tfmt", params.outputFormat.value.get)

      ab.result()
    }

    SesameRdfFormatter.run(sesameRdfFormatterArgs)

    //
    // The SesameRdfFormatter does not support a return code, so we're always returning zero meaning everything
    // always is ok. Which makes it harder to embed the Serializer in scripts, so we need to build in support
    // for return codes but for now its just zero.
    //
    0
  }

  def apply(params: CommandLineParams2) = params.api match {
    case SerializerApiOWLAPI => OwlApiSerializer(params)
    case SerializerApiSesame => runSesameRdfFormatter(params)
  }
}
