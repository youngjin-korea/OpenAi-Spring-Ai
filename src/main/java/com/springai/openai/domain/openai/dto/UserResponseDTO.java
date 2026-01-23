package com.springai.openai.domain.openai.dto;

public record UserResponseDTO(
        String name, Long age, String address, String phoneNumber, String zipCode
) {

}
