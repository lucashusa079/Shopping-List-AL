package com.aaron.shoppinglistal;

public class Item {
    private String id;
    private String name;
    private int quantity;
    private boolean bought; // Este campo se usará para saber si el item ha sido comprado

    public Item() {
        // Constructor vacío requerido para Firebase
    }

    public Item(String id, String name, int quantity, boolean bought) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.bought = bought;
    }

    // Getters y setters
    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}