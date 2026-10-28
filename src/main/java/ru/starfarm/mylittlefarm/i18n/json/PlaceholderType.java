package ru.starfarm.mylittlefarm.i18n.json;


public enum PlaceholderType {

    PLAYER_NAME,
    NPC_NAME,
    CULTURE_INCOME,
    PRICE,
    AFK_INCOME;


    PlaceholderType() {
    }

    public static PlaceholderType of(String value) {
        PlaceholderType placeholderType = null;
        try {
            placeholderType = PlaceholderType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown placeholder: " + value);
        }
        return placeholderType;
    }

}
