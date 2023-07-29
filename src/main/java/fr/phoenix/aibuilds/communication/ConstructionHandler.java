package fr.phoenix.aibuilds.communication;

import fr.phoenix.aibuilds.AIBuilds;
import fr.phoenix.aibuilds.listener.temp.ModifyConstructionListener;
import fr.phoenix.aibuilds.palette.PaletteBlock;
import fr.phoenix.aibuilds.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;


public class ConstructionHandler {
    private final Location startingLocation;
    private int size;
    private final Player player;
    /**
     * The first dimension has 6 rows that represents the 6 builds AI generated.
     */
    double[][] points;
    /**
     * The material of all the blocks that existed before the construction to be able to rollback.
     */
    private final Map<Vector, Material> cachedMaterial = new HashMap<>();
    private final Vector xAxis = new Vector(1, 0, 0);
    private final Vector yAxis = new Vector(0, 1, 0);
    private final Vector zAxis = new Vector(0, 0, 1);

    private double densityThreshold = 0;

    private final boolean includeThreshold;

    private ModifyConstructionListener modifyConstructionListener;


    public ConstructionHandler(Player player, Location startingLocation, int size,boolean includeThreshold) {
        this.player = player;
        this.startingLocation = startingLocation;
        this.size = size;
        this.includeThreshold = includeThreshold;
    }

    public Player getPlayer() {
        return player;
    }

    public void handleResponse(double[][] points) {
        modifyConstructionListener = new ModifyConstructionListener(this,includeThreshold);
        this.points = points;
        generate();
    }

    public void upSize() {
        undoConstruction();
        size += AIBuilds.plugin.configManager.blocksPerUpsize;
        generate();
    }

    public void downSize() {
        undoConstruction();
        size -= AIBuilds.plugin.configManager.blocksPerUpsize;
        generate();
    }

    public void rotateClockWiseAround(Vector axis) {
        undoConstruction();
        double angle = (AIBuilds.plugin.configManager.anglePerRotation * Math.PI) / 180;
        xAxis.rotateAroundAxis(axis, angle);
        yAxis.rotateAroundAxis(axis, angle);
        zAxis.rotateAroundAxis(axis, angle);
        generate();
    }

    public void rotateAntiClockWiseAround(Vector axis) {
        undoConstruction();
        undoConstruction();
        double angle = -(AIBuilds.plugin.configManager.anglePerRotation * Math.PI) / 180;
        xAxis.rotateAroundAxis(axis, angle);
        yAxis.rotateAroundAxis(axis, angle);
        zAxis.rotateAroundAxis(axis, angle);
        generate();
    }

    public void pushFrom(Vector direction) {
        undoConstruction();
        startingLocation.add(direction.multiply(AIBuilds.plugin.configManager.blocksPerTranslation));
        generate();
    }

    public void pullFrom(Vector direction) {
        undoConstruction();
        startingLocation.add(direction.multiply(-AIBuilds.plugin.configManager.blocksPerTranslation));
        generate();
    }

    public void increaseThreshold() {
        undoConstruction();
        densityThreshold += AIBuilds.plugin.configManager.thresholdIncrease;
        generate();
    }

    public void decreaseThreshold() {
        undoConstruction();
        densityThreshold -= AIBuilds.plugin.configManager.thresholdIncrease;
        generate();
    }

    public void acceptConstruction() {
        Message.CONSTRUCTION_ACCEPTED.format().send(player);
        modifyConstructionListener.close();
    }

    public void removeConstruction() {
        Message.CONSTRUCTION_REMOVED.format().send(player);
        undoConstruction();
        modifyConstructionListener.close();
    }


    private void undoConstruction() {
        for (Vector vector : cachedMaterial.keySet()) {
            startingLocation.clone().add(vector).getBlock().setType(cachedMaterial.get(vector));
        }
        cachedMaterial.clear();
    }

    private void generate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(AIBuilds.plugin, () -> {
            int n = points.length;
            for (int i = 0; i < n; i++) {
                if (points[i][6] < densityThreshold) continue;
                //We add 0.5 to each to have numbers between 0 and 1 rather than between -0.5 and 0.5.
                //This vector enables to get to the point at which the block should be modified
                Vector vector = xAxis.clone().multiply(points[i][0])
                        .add(yAxis.clone().multiply(points[i][2]))
                        .add(zAxis.clone().multiply(points[i][1]));
                vector = vector.multiply(size);
                vector = new Vector((int) vector.getX(), (int) vector.getY(), (int) vector.getZ());
                PaletteBlock paletteBlock = AIBuilds.plugin.paletteManager.getPaletteBlock((int) points[i][3], (int) points[i][4], (int) points[i][5]);
                Block block = startingLocation.clone().add(vector).getBlock();
                cachedMaterial.putIfAbsent(vector, block.getType());
                block.setType(paletteBlock.getMaterial());
                block.setBlockData(Bukkit.createBlockData(paletteBlock.getMaterial()));
            }
        });
    }
}
