package com.dh.ondot.member.infra.dto

data class ApplePublicKeyResponse(
    val keys: List<Key>,
) {
    data class Key(
        val kty: String,
        val kid: String,
        val use: String,
        val alg: String,
        val n: String,
        val e: String,
    )

    fun getMatchedKey(kid: String, alg: String): Key? {
        return keys.firstOrNull { it.kid == kid && it.alg == alg }
    }
}
