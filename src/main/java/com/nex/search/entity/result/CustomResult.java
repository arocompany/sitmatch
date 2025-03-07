package com.nex.search.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class CustomResult {
    private String error;

    private List<CustomResults> results;
}
