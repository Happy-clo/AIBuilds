package fr.phoenix.aibuilds.palette;

import com.google.gson.JsonObject;
import org.bukkit.Material;

public class PaletteBlock {
    private final Material material;
    //TODO BlockState
    private final int R, G, B;

    public PaletteBlock(JsonObject jsonObject) {
        material = Material.valueOf(jsonObject.get("material").getAsString());
        R = jsonObject.get("R").getAsInt();
        G = jsonObject.get("G").getAsInt();
        B = jsonObject.get("B").getAsInt();
    }

    public double getDistanceFrom(int r, int g, int b) {
        return Math.sqrt(Math.pow(r - R, 2) + Math.pow(g - G, 2) + Math.pow(b - B, 2));
    }

    public Material getMaterial() {
        return material;
    }
}
