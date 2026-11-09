package pl.zombek.client.core.itemtracker;

/**
 * Accumulated statistics for a specific tracked item during a session.
 * Tracks quantities separated by event type and confidence level.
 */
public final class ItemTrackingStatistics {

    private final String itemId;
    private final String itemName;

    private int currentAmount = 0;
    private int totalCollected = 0;
    private int totalUsed = 0;
    private int totalConsumed = 0;
    private int totalDropped = 0;
    private int totalCrafted = 0;
    private int totalPlaced = 0;
    private int totalUnknownChanges = 0;

    // Confirmed-only counters
    private int confirmedCollected = 0;
    private int confirmedUsed = 0;
    private int confirmedConsumed = 0;
    private int confirmedDropped = 0;
    private int confirmedCrafted = 0;
    private int confirmedPlaced = 0;

    public ItemTrackingStatistics(String itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName != null ? itemName : itemId;
    }

    public synchronized void recordEvent(ItemTrackEvent event) {
        if (event == null) return;
        int delta = event.getDelta();
        int absDelta = Math.abs(delta);
        boolean confirmed = (event.getConfidence() == EventConfidence.CONFIRMED);

        switch (event.getEventType()) {
            case COLLECTED:
            case PICKED_UP:
                totalCollected += absDelta;
                if (confirmed) confirmedCollected += absDelta;
                break;
            case USED:
                totalUsed += absDelta;
                if (confirmed) confirmedUsed += absDelta;
                break;
            case CONSUMED:
                totalConsumed += absDelta;
                if (confirmed) confirmedConsumed += absDelta;
                break;
            case DROPPED:
                totalDropped += absDelta;
                if (confirmed) confirmedDropped += absDelta;
                break;
            case CRAFTED:
                totalCrafted += absDelta;
                if (confirmed) confirmedCrafted += absDelta;
                break;
            case PLACED:
                totalPlaced += absDelta;
                if (confirmed) confirmedPlaced += absDelta;
                break;
            case UNKNOWN_CHANGE:
            default:
                totalUnknownChanges += absDelta;
                break;
        }
    }

    public synchronized void updateCurrentAmount(int amount) {
        this.currentAmount = Math.max(0, amount);
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public synchronized int getCurrentAmount() { return currentAmount; }
    public synchronized int getTotalCollected() { return totalCollected; }
    public synchronized int getTotalUsed() { return totalUsed; }
    public synchronized int getTotalConsumed() { return totalConsumed; }
    public synchronized int getTotalDropped() { return totalDropped; }
    public synchronized int getTotalCrafted() { return totalCrafted; }
    public synchronized int getTotalPlaced() { return totalPlaced; }
    public synchronized int getTotalUnknownChanges() { return totalUnknownChanges; }

    public synchronized int getConfirmedCollected() { return confirmedCollected; }
    public synchronized int getConfirmedUsed() { return confirmedUsed; }
    public synchronized int getConfirmedConsumed() { return confirmedConsumed; }
    public synchronized int getConfirmedDropped() { return confirmedDropped; }
    public synchronized int getConfirmedCrafted() { return confirmedCrafted; }
    public synchronized int getConfirmedPlaced() { return confirmedPlaced; }

    public synchronized int getTotalInflow() {
        return totalCollected + totalCrafted;
    }

    public synchronized int getConfirmedInflow() {
        return confirmedCollected + confirmedCrafted;
    }

    public synchronized int getTotalOutflow() {
        return totalUsed + totalConsumed + totalDropped + totalPlaced;
    }

    public synchronized int getConfirmedOutflow() {
        return confirmedUsed + confirmedConsumed + confirmedDropped + confirmedPlaced;
    }

    public synchronized int getNetSessionChange() {
        return totalCollected + totalCrafted - totalUsed - totalConsumed - totalDropped - totalPlaced;
    }
}
