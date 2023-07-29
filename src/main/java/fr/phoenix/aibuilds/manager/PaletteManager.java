package fr.phoenix.aibuilds.manager;

import com.google.gson.JsonParser;
import fr.phoenix.aibuilds.palette.PaletteBlock;
import fr.phoenix.aibuilds.utils.ConfigFile;

import java.util.ArrayList;
import java.util.List;

public class PaletteManager {
    private final List<PaletteBlock> paletteBlocks = new ArrayList<>();
    private final int[][][] RGBToPalette = new int[256][256][256];


    public void load(boolean clearBefore) {
        if (clearBefore)
            paletteBlocks.clear();

        ConfigFile configFile = new ConfigFile("palette");
        for (String json : configFile.getConfig().getStringList("palette")) {
            paletteBlocks.add(new PaletteBlock(JsonParser.parseString(json).getAsJsonObject()));
        }

        for (int i = 0; i < 256; i++)
            for (int j = 0; j < 256; j++)
                for (int k = 0; k < 256; k++) {
                    double min = Double.MAX_VALUE;
                    int paletteBlockIndex = 0;
                    for (int l = 0; l < paletteBlocks.size(); l++) {
                        double distance = paletteBlocks.get(l).getDistanceFrom(i, j, k);
                        if (distance < min) {
                            min = distance;
                            paletteBlockIndex = l;
                        }
                    }
                    RGBToPalette[i][j][k] = paletteBlockIndex;
                }
    }

    public PaletteBlock getPaletteBlock(int r, int g, int b) {
        return paletteBlocks.get(RGBToPalette[r][g][b]);
    }

}
