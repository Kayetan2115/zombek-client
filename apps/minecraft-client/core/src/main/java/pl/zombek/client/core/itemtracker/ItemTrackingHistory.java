package pl.zombek.client.core.itemtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Ring buffer and query engine for recorded item tracking events.
 */
public final class ItemTrackingHistory {

    private final int maxCapacity;
    private final List<ItemTrackEvent> events = new CopyOnWriteArrayList<>();

    public ItemTrackingHistory(int maxCapacity) {
        this.maxCapacity = Math.max(10, maxCapacity);
    }

    public synchronized void addEvent(ItemTrackEvent event) {
        if (event == null) return;
        events.add(event);
        while (events.size() > maxCapacity) {
            events.remove(0);
        }
    }

    public List<ItemTrackEvent> getAllEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<ItemTrackEvent> getRecentEvents(int limit) {
        int count = Math.min(limit, events.size());
        if (count <= 0) return Collections.emptyList();
        List<ItemTrackEvent> sub = events.subList(events.size() - count, events.size());
        List<ItemTrackEvent> reversed = new ArrayList<>(sub);
        Collections.reverse(reversed); // Most recent first
        return Collections.unmodifiableList(reversed);
    }

    public List<ItemTrackEvent> getFilteredEvents(Set<ItemEventType> types, Set<EventConfidence> confidences) {
        return events.stream()
                .filter(e -> types == null || types.contains(e.getEventType()))
                .filter(e -> confidences == null || confidences.contains(e.getConfidence()))
                .collect(Collectors.toList());
    }

    public synchronized void clear() {
        events.clear();
    }

    public int size() {
        return events.size();
    }

    /**
     * Serializes history into formatted JSON for export.
     */
    public String exportToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray array = new JsonArray();
        for (ItemTrackEvent e : events) {
            JsonObject obj = new JsonObject();
            obj.addProperty("timestamp", e.getTimestampEpoch());
            obj.addProperty("timeFormatted", e.getFormattedTimestamp());
            obj.addProperty("itemId", e.getItemId());
            obj.addProperty("itemName", e.getItemName());
            obj.addProperty("delta", e.getDelta());
            obj.addProperty("eventType", e.getEventType().name());
            obj.addProperty("confidence", e.getConfidence().name());
            obj.addProperty("description", e.getDescription());
            array.add(obj);
        }
        return gson.toJson(array);
    }
}
