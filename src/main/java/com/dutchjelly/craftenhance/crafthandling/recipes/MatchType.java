package com.dutchjelly.craftenhance.crafthandling.recipes;

import lombok.Getter;

public enum MatchType {
    META("match meta"), MATERIAL("only match type"), NAME("only match name and type");

    @Getter
    private String description;

    MatchType(String s) {
        description = s;
    }
}
