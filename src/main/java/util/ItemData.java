package util;

public class ItemData {
    public int itemId;
    public String itemName;

    public ItemData(int itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ItemData)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.itemId == ((ItemData) obj).itemId && this.itemName.equals(((ItemData) obj).itemName);
    }

    @Override
    public int hashCode() {
        return itemId;
    }
}