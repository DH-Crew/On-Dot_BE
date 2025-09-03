package com.dh.ondot.notification.infra.discord;

import com.dh.ondot.member.domain.enums.OauthProvider;
import org.springframework.stereotype.Component;

@Component
public class DiscordMessageTemplate {
    public String createUserRegistrationMessage(String memberEmail, OauthProvider oauthProvider, Long totalMemberCount) {
        return String.format(
                """
                    🎉 **온닷 %,d번 째 신규 사용자 가입!** 🎉
            
                    👤 **사용자 계정**: %s
                    🔐 **가입 방식**: %s
                    👥 **총 사용자 수**: %,d명
                """,
                totalMemberCount,
                memberEmail,
                getOauthProviderDisplayName(oauthProvider),
                totalMemberCount
        );
    }

    private String getOauthProviderDisplayName(OauthProvider provider) {
        return switch (provider) {
            case KAKAO -> "Kakao";
            case APPLE -> "Apple";
            default -> provider.name();
        };
    }
}
