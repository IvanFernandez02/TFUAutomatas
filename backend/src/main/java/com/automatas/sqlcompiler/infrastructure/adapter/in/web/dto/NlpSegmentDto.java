package com.automatas.sqlcompiler.infrastructure.adapter.in.web.dto;

import com.automatas.sqlcompiler.domain.model.NlpSegment;

public record NlpSegmentDto(String text, String label) {
    public static NlpSegmentDto from(NlpSegment segment) {
        return new NlpSegmentDto(segment.text(), segment.label());
    }
}
