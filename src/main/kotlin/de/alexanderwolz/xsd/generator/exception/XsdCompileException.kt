package de.alexanderwolz.xsd.generator.exception

class XsdCompileException(val status: Int, val errors: List<String>, cause: Throwable? = null) :
    Exception("XSD compile failed with errors:\n ${errors.joinToString(separator = "\n")}", cause) {
}