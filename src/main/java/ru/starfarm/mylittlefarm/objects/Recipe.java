package ru.starfarm.mylittlefarm.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.starfarm.mylittlefarm.enums.Product;

import java.util.Map;

@AllArgsConstructor
@Getter
public class Recipe {
    public Map<Product, Integer> ingredients;
    public int time;

    public static Recipe of(Map<Product, Integer> ingredients, int time) {
        return new Recipe(ingredients, time);
    }
}
