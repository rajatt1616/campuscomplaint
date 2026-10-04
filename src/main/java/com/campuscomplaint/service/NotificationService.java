package com.campuscomplaint.service;

import com.campuscomplaint.model.Notification;
import com.campuscomplaint.store.JsonDataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final JsonDataStore store;

    public NotificationService(JsonDataStore store) {
        this.store = store;
    }

    public synchronized void notifyUser(String userId, String complaintId, String message) {
        if (userId == null || userId.isBlank() || message == null || message.isBlank()) {
            return;
        }
        Notification n = new Notification();
        n.setId(store.nextNotificationId());
        n.setUserId(userId);
        n.setComplaintId(complaintId);
        n.setMessage(message);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        store.getNotifications().add(n);
        store.saveNotifications();
    }

    public List<Notification> recent(String userId, int limit) {
        return store.getNotifications().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    public long unreadCount(String userId) {
        return store.getNotifications().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .filter(n -> !n.isRead())
                .count();
    }

    public synchronized void markAllRead(String userId) {
        boolean changed = false;
        for (Notification n : store.getNotifications()) {
            if (userId.equals(n.getUserId()) && !n.isRead()) {
                n.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            store.saveNotifications();
        }
    }
}
