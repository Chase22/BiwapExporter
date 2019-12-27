package io.github.chase22.nina

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

class JsonHashFactory(private val objectMapper: ObjectMapper) {
    fun getHash(json: String): String {
        val newJson = objectMapper
                .readTree(json)
                .fields()
                .asSequence()
                .toMutableList()
                .removeAll { excludedFields.contains(it.key)}

        return sha1(objectMapper.writeValueAsString(newJson))
    }

    private fun sha1(input: String): String {
        val msdDigest = MessageDigest.getInstance("SHA-1")
        msdDigest.update(input.toByteArray(Charset.defaultCharset()), 0, input.length)
        return DatatypeConverter.printHexBinary(msdDigest.digest())
    }

    companion object {
        val excludedFields = listOf("sent")
    }
}