package ru.starfarm.mylittlefarm.utils.livedata;


import java.util.HashSet;
import java.util.Set;


public class LiveData<T> {

    private T data;
    private final Set<Observer<T>> observers = new HashSet<>();

    public synchronized T getValue() {
        return data;
    }

    public synchronized void setValue(T data) {
        this.data = data;
        notifyObservers();
    }

    public void observe(Observer<T> observer) {
        observers.add(observer);
        synchronized (this) {
            if (data != null) {
                observer.onChanged(data);
            }
        }
    }

    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.forEach(observer -> observer.onChanged(data));
    }
}

