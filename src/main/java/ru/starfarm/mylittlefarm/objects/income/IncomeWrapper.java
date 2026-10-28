package ru.starfarm.mylittlefarm.objects.income;

import ru.starfarm.mylittlefarm.utils.livedata.LiveData;
import ru.starfarm.mylittlefarm.utils.livedata.LiveDataWrapper;

public class IncomeWrapper implements LiveDataWrapper<Income> {
    private final LiveData<Income> liveData = new LiveData<>();

    public IncomeWrapper() {
    }

    @Override
    public void update(Income value) {
        liveData.setValue(value);
    }

    @Override
    public LiveData<Income> liveData() {
        return liveData;
    }
}