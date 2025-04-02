package com.dh.ondot.member.infra.dto;

import java.util.List;
import java.util.Optional;

public record ApplePublicKeyResponse(
        List<Key> keys
) {
    public record Key(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ){}

    public Optional<Key> getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(k -> k.kid().equals(kid) && k.alg().equals(alg))
                .findFirst();
    }
}
