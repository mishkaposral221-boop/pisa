package com.example.client.menu.data;

public class Setting {
    public final String name;
    public final Type type;
    public final double min;
    public final double max;
    public double value;
    public boolean boolValue;
    public boolean dragging = false;

    public Setting(String name, double min, double max, double value) {
        this.name = name;
        this.type = Type.SLIDER;
        this.min = min;
        this.max = max;
        this.value = value;
        this.boolValue = false;
    }

    public Setting(String name, boolean value) {
        this.name = name;
        this.type = Type.TOGGLE;
        this.min = 0.0;
        this.max = 1.0;
        this.value = 0.0;
        this.boolValue = value;
    }

    public double getNormalized() {
        if (this.max == this.min) {
            return 0.0;
        }
        return (this.value - this.min) / (this.max - this.min);
    }

    public void setNormalized(double t) {
        this.value = this.min + t * (this.max - this.min);
    }

    public float getFloat() {
        return (float)this.value;
    }

    public int getInt() {
        return (int)Math.round(this.value);
    }

    public boolean getBool() {
        return this.boolValue;
    }

    public boolean isToggle() {
        return this.type == Type.TOGGLE;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double v) {
        this.value = Math.max(this.min, Math.min(this.max, v));
    }

    public void setBool(boolean b) {
        this.boolValue = b;
    }

    public String formatValue() {
        if (this.type == Type.TOGGLE) {
            return this.boolValue ? "ON" : "OFF";
        }
        if (this.max <= 10.0) {
            return String.format("%.1f", this.value);
        }
        return String.valueOf(this.getInt());
    }

        public static enum Type {
        SLIDER,
        TOGGLE;

    }
}

