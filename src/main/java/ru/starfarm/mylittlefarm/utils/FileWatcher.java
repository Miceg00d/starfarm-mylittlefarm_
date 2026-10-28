package ru.starfarm.mylittlefarm.utils;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "MyLittleFarm-Files")
public class FileWatcher {

    private static volatile FileWatcher instance = null;
    private final Map<Path, FileTime> filesLastUpdateTime = new HashMap<>();
    private final PropertyChangeSupport support;

    private FileWatcher() {
        this.support = new PropertyChangeSupport(this);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> filesLastUpdateTime.keySet().forEach(this::checkFile), 5, 5, TimeUnit.SECONDS);
    }

    private void checkFile(Path path) {
        FileTime lastModifiedTime = lastModifiedTime(path);
        try {
            FileTime currentModifiedTime = Files.getLastModifiedTime(path);

            if (!currentModifiedTime.equals(lastModifiedTime)) {
                filesLastUpdateTime.put(path, currentModifiedTime);
                sendUpdate(path);
            }
        } catch (IOException e) {
            log.error("Error monitoring file: " + e.getMessage());
        }
    }

    public void watchFile(Path path) {
        filesLastUpdateTime.put(path, null);
    }

    public FileTime lastModifiedTime(Path filePath) {
        return filesLastUpdateTime.get(filePath);
    }

    public void sendUpdate(Path path) {
        support.firePropertyChange(path.toString(), null, path);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public static FileWatcher getInstance() {
        synchronized (FileWatcher.class) {
            if (instance == null) {
                instance = new FileWatcher();
            }
            return instance;
        }
    }

}
