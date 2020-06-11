package com.dutchjelly.craftenhance;

public interface IMatcher<T> {
    boolean match(T a, T b);
}
