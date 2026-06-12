package com.patelhardware.model;

/**
 * Deliverable 3 – 4a
 * Model class representing a hardware item.
 */
public class Items {

    private int     itemId;
    private String  name;
    private String  color;
    private String  description;
    private double  price;
    private boolean available;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Items() {}

    public Items(int itemId, String name, String color,
                 String description, double price, boolean available) {
        this.itemId      = itemId;
        this.name        = name;
        this.color       = color;
        this.description = description;
        this.price       = price;
        this.available   = available;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getItemId()                      { return itemId; }
    public void setItemId(int itemId)           { this.itemId = itemId; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public String getColor()                    { return color; }
    public void setColor(String color)          { this.color = color; }

    public String getDescription()              { return description; }
    public void setDescription(String desc)     { this.description = desc; }

    public double getPrice()                    { return price; }
    public void setPrice(double price)          { this.price = price; }

    public boolean isAvailable()                { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
