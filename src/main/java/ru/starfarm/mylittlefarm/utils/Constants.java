package ru.starfarm.mylittlefarm.utils;

import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import ru.starfarm.mylittlefarm.enums.*;
import ru.starfarm.mylittlefarm.objects.boosters.Booster;
import ru.starfarm.mylittlefarm.player.info.AnimalData;
import ru.starfarm.mylittlefarm.player.info.EventInfo;
import ru.starfarm.mylittlefarm.player.info.ProductInfo;
import ru.starfarm.mylittlefarm.player.info.SkillInfo;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@UtilityClass
public
class Constants {
    public Type CULTURE_TYPE = (new TypeToken<ConcurrentMap<CultureType, Integer>>() {}).getType();
    public Type ANIMAL_TYPE = (new TypeToken<ConcurrentMap<AnimalType, AnimalData>>() {}).getType();
    public Type PRODUCT_TYPE = (new TypeToken<ConcurrentMap<Product, ProductInfo>>() {}).getType();
    public Type UPGRADE_TYPE = (new TypeToken<ConcurrentMap<UpgradeType, Integer>>() {}).getType();
    public Type DATA_TYPE = (new TypeToken<ConcurrentMap<DataType, Integer>>() {}).getType();
    public Type LONG_DATA_TYPE = (new TypeToken<ConcurrentMap<DataType, Long>>() {}).getType();
    public Type SKILL_TYPE = (new TypeToken<ConcurrentMap<Skill, SkillInfo>>() {}).getType();
    public Type EVENT_TYPE = (new TypeToken<ConcurrentMap<EventType, EventInfo>>() {}).getType();
    public Type TASK_TYPE = (new TypeToken<ConcurrentMap<Integer, Boolean>>() {}).getType();
    public Type INCOME_BOOSTER_TYPE = (new TypeToken<List<Booster>>() {
    }).getType();
}
