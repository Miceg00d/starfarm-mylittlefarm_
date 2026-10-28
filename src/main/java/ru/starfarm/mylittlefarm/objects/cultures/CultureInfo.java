package ru.starfarm.mylittlefarm.objects.cultures;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CultureInfo {
    private double price;
    private double income;
    private List<String> locations;

    public double baseIncome() {
        return this.income;
    }

    public List<String> locations() {
        return this.locations;
    }

    public double price() {
        return this.price;
    }
}
