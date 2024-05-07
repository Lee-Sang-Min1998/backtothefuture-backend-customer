package com.backtothefuture.member.dto.response;

import com.backtothefuture.member.dto.request.ResidenceInfoDto;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MemberInfoDto(
        Long id,
        String authId,
        String email,
        String name,
        String phoneNumber,
        String profile,
        LocalDate birth,
        String gender,
        AccountResponseInfoDto accountInfo,
        ResidenceInfoDto residenceInfo
) {
}
