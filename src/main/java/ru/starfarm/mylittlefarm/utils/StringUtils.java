package ru.starfarm.mylittlefarm.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public String formatDouble(double d) {
        if (d < 1000.0) return String.format("%.2f", d).replace(",", ".");
        if (d < 1_000_000.0) return format(d, 1_000.0, "K");
        if (d < 1_000_000_000.0) return format(d, 1_000_000.0, "M");
        if (d < 1.0E12) return format(d, 1.0E9, "B");
        if (d < 1.0E15) return format(d, 1.0E12, "T");
        if (d < 1.0E18) return format(d, 1.0E15, "Qa");
        if (d < 1.0E21) return format(d, 1.0E18, "Qi");
        if (d < 1.0E24) return format(d, 1.0E21, "Sx");
        if (d < 1.0E27) return format(d, 1.0E24, "Sp");
        if (d < 1.0E30) return format(d, 1.0E27, "Oc");
        if (d < 1.0E33) return format(d, 1.0E30, "No");

        return String.format("%.2f", d / 1.0E33).replace(",", ".") + "Dc";
    }

    private String format(double value, double divider, String suffix) {
        return String.format("%.2f", value / divider).replace(",", ".") + suffix;
    }

    public String format(double d) {
        return formatDouble(d);
    }

    public String fixNumerical(int num, String... arr) {
        int preLastDigit = num % 100 / 10;
        if (preLastDigit == 1) {
            return String.format("%d %s", num, arr[2]);
        }

        int lastDigit = num % 10;
        switch (lastDigit) {
            case 1:
                return String.format("%d %s", num, arr[0]);
            case 2:
            case 3:
            case 4:
                return String.format("%d %s", num, arr[1]);
            default:
                return String.format("%d %s", num, arr[2]);
        }
    }
}