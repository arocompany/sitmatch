package com.nex.Chart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public interface TraceHistDto {
    String getTraceDate();
    String getUserId();
    int getTraceCnt();
}
