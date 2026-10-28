package ru.starfarm.mylittlefarm.top.leader;

import lombok.Data;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Data
public class Leader {
    private final String title;

    private final String format;

    private final String table;

    private final String row;

    private List<Content> contents = Collections.emptyList();

    public void update() {
        MyLittleFarm.getInstance().getHandler().getExecuteHandler().executeQuery(true, "SELECT `name`, `" + this.row + "` FROM `" + this.table + "` ORDER BY `" + this.row + "` DESC LIMIT 10 ", new Object[0]).thenAccept((rs) -> {
            LinkedList<Content> contents = new LinkedList<>();

            while (rs.next() && contents.size() < 10) {
                String name = rs.getString("name");
                contents.add(new Content(format(contents.size()), name, row.equals("rebirths") ? String.valueOf(rs.getObject(this.row)) : StringUtils.format(rs.getDouble(row))));
            }

            this.contents = contents;
        });
    }

    public static String format(int pos) {
        return (pos == 0 ? "§c" : (pos == 1 ? "§e" : (pos == 2 ? "§6" : "§a"))) + (pos + 1);
    }

}
