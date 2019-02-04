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

package org.edmcouncil.rdf_toolkit.command

import de.derivo.sparqldlapi.Query
import de.derivo.sparqldlapi.QueryEngine
import de.derivo.sparqldlapi.QueryResult
import de.derivo.sparqldlapi.exceptions.QueryEngineException
import de.derivo.sparqldlapi.exceptions.QueryParserException
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory

/**
 * The SparqlDlQueryEngine is used to query the Configuration File (which is an ontology) with SPARQL-DL
 */
class SparqlDlQueryEngine(manager: OWLOntologyManager, reasoner: OWLReasoner) {

  //
  // Create an instance of the SPARQL-DL query engine
  //
  lazy val engine = QueryEngine.create(manager, reasoner)

  // Some queries which demonstrate more sophisticated language constructs of SPARQL-DL

  // The empty ASK is true by default
  processQuery(
    "ASK {}"
  )

  def processQuery(q: String) = {
    try {
      val startTime = System.currentTimeMillis()

      // Create a SPARQL-DL query
      val query = Query.create(q)

      System.out.println("Excecute query:")
      System.out.println(q)
      System.out.println("-------------------------------------------------")

      // Execute the query and generate the result set
      val result = engine.execute(query)

      if (query.isAsk()) {
        System.out.print("Result: ")
        if (result.ask()) {
          System.out.println("yes")
        } else {
          System.out.println("no")
        }
      } else {
        if (!result.ask()) {
          System.out.println("Query has no solution.\n")
        } else {
          System.out.println("Results:")
          System.out.print(result)
          System.out.println("-------------------------------------------------")
          System.out.println("Size of result set: " + result.size())
        }
      }

      System.out.println("-------------------------------------------------")
      System.out.println("Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + "s\n")
    } catch {
      case e: QueryParserException ⇒ System.out.println("Query parser error: " + e)
      case e: QueryEngineException ⇒ System.out.println("Query engine error: " + e)
    }
  }
}
