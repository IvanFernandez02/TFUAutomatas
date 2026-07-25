package com.automatas.semantico.infrastructure.adapter.in.web.dto;

import com.automatas.shared.domain.model.NlpSegment;

public record NlpSegmentDto(String text, String label) {
    public static NlpSegmentDto from(NlpSegment segment) {
        return new NlpSegmentDto(segment.text(), segment.label());
    }
}
