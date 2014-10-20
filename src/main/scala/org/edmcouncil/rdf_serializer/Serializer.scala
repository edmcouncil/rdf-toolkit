package org.edmcouncil.rdf_serializer


class Serializer(private val commands: SerializerCommands) {


  def run: Int = {

    val rc1 = commands.validateParams
    if (rc1 > 0) return rc1

    val rc2 = OwlApiSerializer(commands)
    if (rc2 > 0) return rc2

    0
  }

}

object Serializer {

  def apply(params: CommandLineParams) = new Serializer(new SerializerCommands(params))
}