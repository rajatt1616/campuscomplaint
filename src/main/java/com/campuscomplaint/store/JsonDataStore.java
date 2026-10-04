package com.campuscomplaint.store;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.Notification;
import com.campuscomplaint.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonDataStore {

    private final ObjectMapper mapper;
    private final Path dir;
    private List<User> users = new ArrayList<>();
    private List<Complaint> complaints = new ArrayList<>();
    private List<Notification> notifications = new ArrayList<>();

    public JsonDataStore(ObjectMapper mapper, @Value("${app.data.dir}") String dataDir) {
        this.mapper = mapper;
        this.dir = Paths.get(dataDir);
    }

    @PostConstruct
    public void load() {
        try {
            Files.createDirectories(dir);
            Path usersFile = dir.resolve("users.json");
            Path complaintsFile = dir.resolve("complaints.json");
            if (Files.exists(usersFile)) {
                users = mapper.readValue(usersFile.toFile(), new TypeReference<List<User>>() {
                });
            }
            if (Files.exists(complaintsFile)) {
                complaints = mapper.readValue(complaintsFile.toFile(), new TypeReference<List<Complaint>>() {
                });
            }
            Path notificationsFile = dir.resolve("notifications.json");
            if (Files.exists(notificationsFile)) {
                notifications = mapper.readValue(notificationsFile.toFile(),
                        new TypeReference<List<Notification>>() {
                        });
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load data files from " + dir, e);
        }
    }

    public synchronized void saveUsers() {
        try {
            mapper.writer()
                    .forType(mapper.getTypeFactory().constructCollectionType(List.class, User.class))
                    .withDefaultPrettyPrinter()
                    .writeValue(dir.resolve("users.json").toFile(), users);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save users.json", e);
        }
    }

    public synchronized void saveComplaints() {
        write(dir.resolve("complaints.json"), complaints);
    }

    public synchronized void saveNotifications() {
        write(dir.resolve("notifications.json"), notifications);
    }

    public synchronized String nextNotificationId() {
        return String.format("N-%05d", notifications.size() + 1);
    }

    private void write(Path file, Object value) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), value);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save " + file, e);
        }
    }

    public synchronized String nextComplaintId() {
        int next = complaints.size() + 1;
        return String.format("CMP-%04d", next);
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
