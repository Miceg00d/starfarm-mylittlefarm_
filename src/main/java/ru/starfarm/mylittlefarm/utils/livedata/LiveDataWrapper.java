package ru.starfarm.mylittlefarm.utils.livedata;

public interface LiveDataWrapper<T> {

    void update(T value);

    LiveData<T> liveData();

}
