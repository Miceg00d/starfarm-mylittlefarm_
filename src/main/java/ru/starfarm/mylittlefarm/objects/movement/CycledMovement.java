package ru.starfarm.mylittlefarm.objects.movement;

import lombok.Getter;
import org.bukkit.Location;
import ru.starfarm.core.entity.FakeEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class CycledMovement {

    private final List<Movement.MovementBuilder> points;
    private final FakeEntity entity;
    private Movement movement;
    private int nextIndex = 0;

    public @Getter boolean process = true;
    public Consumer<FakeEntity> stopHandler;

    public CycledMovement(FakeEntity entity, Location... locations) {
        this.entity = entity;
        this.points = new LinkedList<>();
        for (Location location : locations)
            points.add(MovementUtils.movement().destination(location).speed(0.1).entity(entity).physic(true));

        this.points.forEach(point -> {
            point.entity(entity).stopHandler(m -> {
                if (process)
                    runNext();
            });
        });

        runNext();
    }

    public CycledMovement(FakeEntity entity, Consumer<FakeEntity> stopHandler, Location... locations) {
        this.entity = entity;
        this.points = new LinkedList<>();
        this.stopHandler = stopHandler;
        for (Location location : locations)
            points.add(MovementUtils.movement().destination(location).speed(0.1).entity(entity).physic(true));

        this.points.forEach(point -> {
            point.entity(entity).stopHandler(m -> {
                if (process)
                    runNext();
            });
        });

        runNext();
    }

    public void runNext() {
        movement = points.get(nextIndex).build();
        if (++nextIndex > points.size() - 1) {
            nextIndex = 0;
            process = false;
            if (stopHandler != null)
                stopHandler.accept(entity);
        }
    }

}
