package com.challengeteam.shop.entity.phone;

import lombok.Getter;

@Getter
public enum PhoneColor {
    BLACK("Black", "#000000"),
    WHITE("White", "#FFFFFF"),
    GRAY("Gray", "#808080"),
    SILVER("Silver", "#C0C0C0"),
    GOLD("Gold", "#FFD700"),
    RED("Red", "#FF0000"),
    BLUE("Blue", "#0000FF"),
    GREEN("Green", "#008000"),
    YELLOW("Yellow", "#FFFF00"),
    PINK("Pink", "#FFC0CB");

    private final String displayName;
    private final String hexCode;

    PhoneColor(String displayName, String hexCode) {
        this.displayName = displayName;
        this.hexCode = hexCode;
    }

    @Override
    public String toString() {
        return displayName;
    }
}