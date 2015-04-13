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
}

object EdmCouncilVersionIRI {

  def apply(iri: IRI): EdmCouncilVersionIRI = new EdmCouncilVersionIRI(iri)

  def apply(iri: String): EdmCouncilVersionIRI = apply(IRI.create(iri))
}
