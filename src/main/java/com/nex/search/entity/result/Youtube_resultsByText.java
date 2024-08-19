package com.nex.search.entity.result;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Data
public class Youtube_resultsByText {
    private String position_on_page;
    private String title;
    private String link;
    private Map<String, String> thumbnail;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Youtube_resultsByText that = (Youtube_resultsByText) o;
        return link == that.link;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}