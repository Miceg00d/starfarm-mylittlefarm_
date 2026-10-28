package ru.starfarm.mylittlefarm.player.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

@AllArgsConstructor
@Getter @Setter
public class ProductInfo {
    public int count;
    public Map<Integer, Integer> queue;

    public void addQueue(int time) {
        if (queue.isEmpty()) {
            queue.put(0, time);
            return;
        }
        val id = Collections.max(queue.entrySet(), Comparator.comparingInt(Map.Entry::getKey));
        queue.put(id.getKey() + 1, time);
    }
}
