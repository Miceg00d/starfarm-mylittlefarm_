package ru.starfarm.mylittlefarm.top.leader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.server.v1_12_R1.EnumDirection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import ru.starfarm.core.CorePlugin;
import ru.starfarm.core.util.bukkit.LocationUtil;
import ru.starfarm.mylittlefarm.MyLittleFarm;
import ru.starfarm.mylittlefarm.top.BannerBoard;
import ru.starfarm.mylittlefarm.top.BoardAPI;
import ru.starfarm.mylittlefarm.top.BoardRenderer;
import ru.starfarm.mylittlefarm.utils.config.BaseConfiguration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;
import java.util.*;

public class LeaderManager extends BaseConfiguration {
    private BannerBoard board;

    private List<Leader> leaders;

    private final Image background;

    private BukkitTask boardTask;

    @SneakyThrows
    public LeaderManager(CorePlugin corePlugin) {
        super(corePlugin, "leaders.yml");
        BoardAPI.init(corePlugin);
        this.background = ImageIO.read(MyLittleFarm.getInstance().getResource("background.png"));
    }

    public void update() {
        this.leaders.forEach(Leader::update);
        this.boardTask = Bukkit.getScheduler().runTaskLaterAsynchronously(MyLittleFarm.getInstance(), this::updateBoard, 100L);
    }

    private void updateBoard() {
        System.out.println("[MLF] Вызываю обновление доски...");
        Objects.requireNonNull(this.board, "board == null");
        BoardRenderer renderer = this.board.getRenderer();
        Graphics2D graphics = renderer.createGraphics();

        for(int i = 0; i < this.leaders.size(); ++i) {
            graphics.drawImage(this.background, 384 * i, 0, null);
            Leader leader = leaders.get(i);
            graphics.setFont(graphics.getFont().deriveFont(0, 28.0F));
            renderer.drawString(leader.getTitle(), i * 128 * 3 + 192 - graphics.getFontMetrics().stringWidth(ChatColor.stripColor(leader.getTitle())) / 2, 45, Color.WHITE);
            graphics.setFont(graphics.getFont().deriveFont(0, 20.0F));

            for (int j = 0; j < 10; ++j) {
                Content content = leader.getContents().size() - 1 < j ? null : leader.getContents().get(j);
                String text = (content == null) ? "§7§oЗагрузка..." : String.format(leader.getFormat(), content.getPos(), content.getName(), content.getValue());
                int xPos = i * 124 * 3 + 192 - graphics.getFontMetrics().stringWidth(ChatColor.stripColor(text)) / 2;

                renderer.drawString(text, xPos, 80 + 39 * j, Color.WHITE);

            }
        }

        graphics.dispose();
        this.board.updateFragments();

        System.out.println("[MLF] Отрисовка завершена успешно!");
    }


    @Override
    protected void onInstall(@NonNull FileConfiguration fileConfiguration) {
        if (this.leaders == null)
            this.leaders = new ArrayList<>();
        if (this.boardTask != null)
            this.boardTask.cancel();
        this.leaders.clear();
        ConfigurationSection leaders = fileConfiguration.getConfigurationSection("leaders");
        Objects.requireNonNull(leaders);
        leaders.getKeys(false).forEach(id -> {
            ConfigurationSection sec = leaders.getConfigurationSection(id);
            String table = sec.getString("table");
            String row = sec.getString("row");
            String title = sec.getString("title");
            String format = sec.getString("format");
            this.leaders.add(new Leader(title, format, table, row));
        });
        System.out.println("[MLF] Пытаюсь создать топ...");
        System.out.println("[MLF] Локация из конфига: " + fileConfiguration.getString("location"));
        System.out.println("[MLF] Загружено лидеров: " + this.leaders.size());

        Location location = LocationUtil.fromString(fileConfiguration.getString("location"), Bukkit.getWorlds().get(0).getSpawnLocation());
        EnumDirection direction = EnumDirection.valueOf(fileConfiguration.getString("direction"));

        if (this.board != null)
            BoardAPI.getApi().unregister(this.board);

        this.board = BoardAPI.createBanner(location, direction, this.leaders.size() * 3, 4);
        System.out.println("[MLF] Баннер создан на " + location.toString());
    }

    public Map<String, Double> getTopFromCriteria(String criteria, int amount) {
        val top = new LinkedHashMap<String, Double>();
        MyLittleFarm.getInstance().getHandler().getExecuteHandler().executeQuery(true, "SELECT * FROM `mlf_players` ORDER BY " + criteria + " DESC LIMIT " + amount).thenAccept(resultSet -> {
            while (resultSet.next()) {
                top.put(resultSet.getString("name"), resultSet.getDouble(criteria));
            }
        });
        return top;
    }
}