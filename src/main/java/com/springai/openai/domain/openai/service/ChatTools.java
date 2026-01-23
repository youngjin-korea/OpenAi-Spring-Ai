package com.springai.openai.domain.openai.service;

import com.springai.openai.domain.openai.dto.UserResponseDTO;
import org.springframework.ai.tool.annotation.Tool;

public class ChatTools {

    @Tool(description = "User personal information : name, age, address, phone, etc")
    public UserResponseDTO getUserInfoTool () {
        return new UserResponseDTO("김영진", 15L, "안산시 단원구", "010-1234-1234", "04043");
    }

//    @Tool(description = "자동차, 금속, 목재 등 산업군별 공정에 적합한 태양연마 제품 스펙(입도, 소재, 용도)을 추천합니다.")
//    public AbrasiveSpecResponse getAbrasiveRecommendation(AbrasiveSpecRequest request) {
//        // 실제 로직: DB(Oracle)에서 조회하거나 비즈니스 로직 수행
//        return abrasiveService.findBestSpec(request);
//    }

//    public record AbrasiveSpecRequest(
//            @JsonProperty(required = true, value = "industryType")
//            String industryType, // 예: 자동차, 목재, 금속, 비철금속
//
//            @JsonProperty(required = true, value = "material")
//            String material, // 구체적인 연마 대상물 (예: 탄소강, 알루미늄, 하이그로시 도장)
//
//            @JsonProperty(value = "processType")
//            String processType // 공정 단계 (예: 거친 연마, 중간 연마, 광택)
//    ) {}

//    public record AbrasiveSpecResponse(
//            String productSeries,  // 제품 시리즈 (예: R203, L338)
//            String grainType,      // 연마재 재질 (예: 알루미늄 옥사이드, 지르코니아)
//            String gritRange,      // 권장 입도 범위 (예: P80 ~ P400)
//            String backingMaterial,// 기재 (예: 종이, 천, 필름)
//            String feature         // 주요 장점 (예: 目詰まり(눈먹힘) 방지 처리, 강력한 절삭력)
//    ) {}
}
