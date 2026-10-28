package ru.starfarm.mylittlefarm.objects.skills;

import com.google.common.collect.Lists;

import java.util.List;

public class FishingRegistry {
    public List<Require> fishingRod = Lists.newArrayList(
            new Require(
                    1_000_000_000_000_000.0, 10
            ),
            new Require(
                    100_000_000_000_000_000.0, 20
            )
    );
    public List<Require> fishingLine = Lists.newArrayList(
            new Require(
                    1_000_000_000_000_000.0, 10, 100
            ),
            new Require(
                    100_000_000_000_000_000.0, 20, 260
            )
    );
    public List<Hook> hookList = Lists.newArrayList();

    public FishingRegistry() {

    }

    public Hook getHook(int level) {
        return hookList.get(level);
    }

    public Require getFishingRod(int level) {
        return fishingRod.get(level);
    }

    public Require getFishingLine(int level) {
        return fishingLine.get(level);
    }
}
