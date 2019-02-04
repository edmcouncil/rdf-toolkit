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

package org.edmcouncil.fibo

import org.semanticweb.owlapi.model.IRI

/**
 * <blockquote>
 * <code>
 * https://spec.edmcouncil.org/<family>/<domain>/<module1..n>/<ontology>
 * </code>
 * </blockquote>
 */
trait EdmCouncilAbstractOntologyIRI {

  def baseIRI: IRI

  def ontologyIRI: IRI

  def versionIRI(version: String) = EdmCouncilVersionIRI(ontologyIRI, version).iri

  def family: String

  def domain: String

  def domainUC: String

  def modules: Seq[String]

  def firstModule = modules.head

  def ontology: String

  def iri: IRI
}

/**
 * <blockquote>
 * <code>
 * https://spec.edmcouncil.org/<family>/<tag|branch>/<domain>/<module1..n>/<ontology>
 * </code>
 * </blockquote>
 */
trait EdmCouncilAbstractVersionIRI extends EdmCouncilAbstractOntologyIRI {

  /**
   * @return the tag or branch or sha-1
   */
  def version: String
}

class EdmCouncilOntologyIRI private (val iri: IRI) extends EdmCouncilAbstractOntologyIRI {

  import org.edmcouncil._

  val splittedIRI = iri.toString.replaceFirst(edmcBaseIRI.toString, "").split('/')

  assert(splittedIRI.length >= 4, s"splitted IRI is ${splittedIRI.mkString(",")}")

  val baseIRI = edmcBaseIRI

  val ontologyIRI = iri

  val family = splittedIRI.head

  val domain = splittedIRI.drop(1).head.toLowerCase

  val domainUC = domain.toUpperCase

  val ontology = splittedIRI.takeRight(1).head

  val modules = splittedIRI.drop(2).dropRight(1).toSeq
}

object EdmCouncilOntologyIRI {

  def apply(iri: IRI): EdmCouncilOntologyIRI = new EdmCouncilOntologyIRI(iri)

  def apply(iri: String): EdmCouncilOntologyIRI = apply(IRI.create(iri))
}

class EdmCouncilVersionIRI private (val iri: IRI) extends EdmCouncilAbstractVersionIRI {

  import org.edmcouncil._

  val splittedIRI = iri.toString.replaceFirst(edmcBaseIRI.toString, "").split('/')

  assert(splittedIRI.length >= 5, s"splitted IRI is ${splittedIRI.mkString(",")}")

  val baseIRI = edmcBaseIRI

  val family = splittedIRI.head

  val domain = splittedIRI.drop(1).head.toLowerCase

  val version = splittedIRI.drop(2).head

  def withVersion(version: String) = EdmCouncilVersionIRI(
    s"${baseIRI.toString}$family/$domainUC/$version/${modules.mkString("/")}/$ontology"
  )

  val domainUC = domain.toUpperCase

  val ontology = splittedIRI.takeRight(1).head

  val modules = splittedIRI.drop(3).dropRight(1).toSeq

  val ontologyIRI = IRI.create(
    s"${baseIRI.toString}$family/$domainUC/${modules.mkString("/")}/$ontology"
  )
}

object EdmCouncilVersionIRI {

  def apply(iri: IRI): EdmCouncilVersionIRI = new EdmCouncilVersionIRI(iri)

  def apply(iri: String): EdmCouncilVersionIRI = apply(IRI.create(iri))

  def apply(ontologyIRI: IRI, version: String): EdmCouncilVersionIRI = {
    val iri = EdmCouncilOntologyIRI(ontologyIRI)
    EdmCouncilVersionIRI(
      s"${iri.baseIRI.toString}${iri.family}/${iri.domainUC}/$version/${iri.modules.mkString("/")}/${iri.ontology}"
    )
  }
}
