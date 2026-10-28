package ru.starfarm.mylittlefarm.objects.movement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import ru.starfarm.core.entity.FakeEntity;
import ru.starfarm.core.task.GlobalTaskContext;
import ru.starfarm.core.task.Task;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

@Getter
public class Movement {

    protected final FakeEntity entity;
    protected final Location destination;
    protected final double speed;
    protected final boolean physic;
    protected final Consumer<Movement> stopHandler;

    protected final Queue<Vector> points;

    protected final Task scheduler;

    public Movement(FakeEntity entity, Location destination, double speed, boolean physic, Consumer<Movement> stopHandler) {
        this.entity = entity;
        this.destination = destination;
        this.speed = speed;
        this.physic = physic;
        this.stopHandler = stopHandler;

        points = new LinkedList<>();

        calculatePoints();

        scheduler = GlobalTaskContext.getContext().every(0, 1, this::onTick);
    }

    public void onTick(Task task) {
        if (!points.isEmpty()) {
            Vector move = points.poll();
            boolean jump = false;
            if (physic && MovementUtils.isInGround(entity)) {
                jump = true;
                double toUpGround = MovementUtils.calcToUpGround(entity);
                double y = Math.min(1.05, toUpGround);
                move.setY(y);
            }
            if (physic && !MovementUtils.isOnGround(entity) && !jump) {
                double toGround = MovementUtils.calcToDownGround(entity);
                double y = .3 > toGround ? -toGround : -.3;
                move.setY(y);
            }
            MovementUtils.move(move, entity);
            MovementUtils.look(destination, entity);
        } else stop();
    }


    protected void calculatePoints() {
        double x = entity.getLocation().getX();
        double y = entity.getLocation().getY();
        double z = entity.getLocation().getZ();

        double dx = destination.getX();
        double dy = destination.getY();
        double dz = destination.getZ();

        double mx = dx - x;
        double my = !physic ? dy - y : 0;
        double mz = dz - z;

        while (mx != 0 || my != 0 || mz != 0) {

            double vx = MovementUtils.calcMove(mx, speed);
            double vy = MovementUtils.calcMove(my, speed);
            double vz = MovementUtils.calcMove(mz, speed);

            mx -= vx;
            my -= vy;
            mz -= vz;

            points.add(new Vector(vx, vy, vz));
        }
    }

    public void stop() {
        scheduler.cancel();
        if (stopHandler != null) stopHandler.accept(this);
    }

    public boolean isProcess() {
        return !scheduler.isCancelled();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public static class MovementBuilder {

        protected FakeEntity entity;
        protected Location destination;
        protected double speed = 0.2;
        protected boolean physic = false;
        protected Consumer<Movement> stopHandler = m -> {
        };

        public Movement build() {
            return new Movement(entity, destination, speed, physic, stopHandler);
        }

    }

}
