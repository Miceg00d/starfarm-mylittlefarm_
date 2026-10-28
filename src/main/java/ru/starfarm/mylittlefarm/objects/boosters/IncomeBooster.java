package ru.starfarm.mylittlefarm.objects.boosters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.ToString;
import ru.starfarm.mylittlefarm.enums.CultureType;
import ru.starfarm.mylittlefarm.objects.income.CultureIncome;

import java.util.Objects;
import java.util.UUID;

@ToString
public class IncomeBooster implements Booster {

    @SerializedName("id")
    private UUID id;
    @SerializedName("playerName")
    private String playerName;
    @SerializedName("culture")
    private String culture;
    @SerializedName("startedTime")
    private long startedTime;
    @SerializedName("modifier")
    private double modifier;
    @SerializedName("duration")
    private long duration;

    @Expose
    private final transient IncomeBoostersSchedule schedule = IncomeBoostersSchedule.instance();

    public IncomeBooster(UUID id, String playerName, String culture, long startedTime, double modifier, long duration) {
        this.id = id;
        this.playerName = playerName;
        this.culture = culture;
        this.startedTime = startedTime;
        this.modifier = modifier;
        this.duration = duration;
    }

    public IncomeBooster() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncomeBooster that = (IncomeBooster) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public String playerName() {
        return this.playerName;
    }

    @Override
    public double modifier() {
        return this.modifier;
    }

    @Override
    public long startedTime() {
        return this.startedTime;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public long expireTime() {
        return System.currentTimeMillis() + duration;
    }

    @Override
    public void reduceDuration(long millis) {
        this.duration -= millis;
    }

    @Override
    public void increaseDuration(long millis) {
        this.duration += millis;
    }

    @Override
    public void startBooster(CultureIncome cultureIncome) {
        schedule.addBooster(this, cultureIncome);
    }

    @Override
    public void stopBooster(CultureIncome cultureIncome) {
        cultureIncome.removeBooster(this);
    }


    @Override
    public boolean isExpired() {
        return duration <= 0;
    }

    @Override
    public CultureType cultureType() {
        return CultureType.valueOf(culture.toUpperCase());
    }

    @Override
    public long generalTimeInOfflineMillis(long offlineTime) {
        return offlineTime >= duration ? this.duration / 1000 : offlineTime / 1000;
    }
}
