package ru.starfarm.mylittlefarm.objects.achievements;

import org.jetbrains.annotations.NotNull;
import ru.starfarm.core.profile.achievement.CoreAchievement;

import java.util.Arrays;
import java.util.List;

public enum GameAchievement implements CoreAchievement {

    MLF_START("Начало начал", "Получите доход 1000$"),
    MLF_NOT_POOR("Уже не бедный", "Получите доход в 50 тыс.$"),
    MLF_HERBALIST("Травник", "Скосите 10000 травы"),
    MLF_VISIT("Фермеры по соседству", "Сходите к 5 различным игрокам на их ферму"),
    MLF_MAX("Максималочка", "Улучшите одну любую культуру в \"Улучшениях\" до максимума"),
    MLF_HALF("Больше чем половина!", "Достигните 50% целей"),
    MLF_BUILDING("Повышаем комфорт", "Приобретите любое здание"),
    MLF_VEGETABLE_GROWLE("Овощевод", "Приобретите по сотне картошки и моркови"),
    MLF_PERFECT_COMBINATION("Прекрасное сочетание", "Вырастите и сто арбузов, и сто тыкв"),
    MLF_MUSHROOMS("Помешанный на грибах", "Обзаведитесь сотней красных и бурых грибов"),
    MLF_FAMOUS_GARDENER("Всемирно известный садовод", "Посадите сотню роз и ландышей"),
    MLF_MASTER_OF_ANIMAL("Мастер над животинкой", "Приобретите максимальное количество кур и кроликов"),
    MLF_UNDER_THE_MEAT("Под мясцо", "Приобретите максимальное количество овец и свиней"),
    MLF_GOURMEAT("Гурман", "Приобретите максимальное количество коров и лошадей"),
    MLF_PICNIC("Летний пикник", "Скушайте кусочек торта"),
    MLF_DROUGHT("Нужно больше воды!", "Играйте от начала и до конца в течения события \"Засуха\""),
    MLF_BLOOM("Вечное цветение", "Играйте от начала и до конца в течения события \"Цветение\""),
    MLF_LEGENDARY_FARMER("Легендарный фермер", "Достигните максимального уровня у всех культур"),
    ;
//    MLF_SPECIAL_DAY("Особый день", "Пройти особый ивент 16 ноября и получить тортик", "в честь дня рождения DEV xFenilafs_ разработчика MLF!");


    public final String displayName;
    public final List<String > description;

    GameAchievement(String displayName, String... description) {
        this.displayName = displayName;
        this.description = Arrays.asList(description);
    }

    @NotNull
    @Override
    public List<String> getDescription() {
        return description;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    @Override
    public String getId() {
        return name();
    }

    @NotNull
    @Override
    public String  getSection() {
        return "MLF";
    }
}