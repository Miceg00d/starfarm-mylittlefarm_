package ru.starfarm.mylittlefarm.utils.livedata;

@FunctionalInterface
public interface Observer<T> {
    void onChanged(T t);
}
