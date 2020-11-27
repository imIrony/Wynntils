/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.webapi.profiles.item.enums;

import net.minecraft.util.text.TextFormatting;

public enum MajorIdentification {

    PLAGUE("Plague", "Poisoned mobs spread 70% of their poison to nearby mobs"),
    HAWKEYE("Hawkeye", "Arrow storm fires 5 arrows each dealing 120% damage"),
    GREED("Greed", "Picking up emeralds heals you and nearby players for 15% max health"),
    CAVALRYMAN("Cavalryman", "You may cast spells and attack with a 70% damage penalty while on a horse"),
    GUARDIAN("Guardian", "50% of damage taken by nearby allies is redirected to you"),
    ALTRUISM("Heart of the Pack", "Nearby players gain 35% of the health you naturally regenerate"),
    HERO("Saviour's Sacrifice", "While under 30% maximum health, nearby allies gain 50% bonus damage and defence"),
    ARCANES("Transcendence", "50% chance for spells to cost no mana when casted"),
    ENTROPY("Entropy", "Meteor falls three times faster"),
    ROVINGASSASSIN("Roving Assassin", "Vanish no longer drains mana while invisible"),
    MAGNET("Magnet", "Pulls items within an 8 block radius towards you"),
    MADNESS("Madness", "Casts a random ability every 3 seconds"),
    LIGHTWEIGHT("Lightweight", "You no longer take fall damage"),
    SORCERY("Sorcery", "30% chance for spells and attacks to cast a second time at no additional cost"),
    TAUNT("Taunt", "Mobs within 12 blocks target you upon casting War Scream");

    String name, description;

    MajorIdentification(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String asLore() {
        return TextFormatting.AQUA + "+" + name + ": " + TextFormatting.DARK_AQUA + description;
    }

}
