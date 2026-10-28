package ru.starfarm.mylittlefarm.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Validator {

    public <T> T getOrNull(T nullable, Supplier<T> else_) {
        return nullable == null ? else_.get() : nullable;
    }

}
