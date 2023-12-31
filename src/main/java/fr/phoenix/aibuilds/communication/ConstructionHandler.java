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
    private final int FACE_PRECISION = 5;
    private final Location startingLocation;
    private int size;
    private final Player player;
    /**
     * The first dimension has 6 rows that represents the 6 builds AI generated.
     */

    private int currentBatchIndex = 0;
    private int numberBatches;
    double[][][] points;

    int[][][] faces;
    /**
     * The material of all the blocks that existed before the construction to be able to rollback.
     */
    private final Map<Vector, Material> cachedMaterial = new HashMap<>();
    private final Vector xAxis = new Vector(1, 0, 0);
    private final Vector yAxis = new Vector(0, 1, 0);
    private final Vector zAxis = new Vector(0, 0, 1);

    private ModifyConstructionListener modifyConstructionListener;


    public ConstructionHandler(Player player, Location startingLocation, int size) {
        this.player = player;
        this.startingLocation = startingLocation;
        this.size = size;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentBatchIndex() {
        return currentBatchIndex;
    }

    public int getNumberBatches() {
        return numberBatches;
    }

    //TODO: Include this in the constructor to have final fields.
    public void handleResponse(double[][][] points, int[][][] faces) {
        this.points = points;
        this.faces = faces;
        numberBatches = points.length;
        modifyConstructionListener = new ModifyConstructionListener(this);
        generate();
    }

    public void nextBatch() {
        undoConstruction();
        currentBatchIndex = (currentBatchIndex + 1) % numberBatches;
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

    private Vector getVectorFromPoints(int i) {
        Vector vector = xAxis.clone().multiply(points[currentBatchIndex][i][0])
                .add(yAxis.clone().multiply(points[currentBatchIndex][i][2]))
                .add(zAxis.clone().multiply(points[currentBatchIndex][i][1]));
        vector = vector.multiply(size);
        return new Vector((int) vector.getX(), (int) vector.getY(), (int) vector.getZ());
    }

    private Vector getColorFromPoints(int i) {
        return new Vector((int) points[currentBatchIndex][i][3], (int) points[currentBatchIndex][i][4], (int) points[currentBatchIndex][i][5]);
    }

    private void generate() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(AIBuilds.plugin, () -> {
                    int n = points[currentBatchIndex].length;
                    for (int i = 0; i < n; i++) {
                        Vector vector = getVectorFromPoints(i);
                        //We add 0.5 to each to have numbers between 0 and 1 rather than between -0.5 and 0.5.
                        //This vector enables to get to the point at which the block should be modified

                        //We don't want to modify the same block twice.
                        if (cachedMaterial.containsKey(vector))
                            continue;
                        PaletteBlock paletteBlock = AIBuilds.plugin.paletteManager.getPaletteBlock((int) points[currentBatchIndex][i][3], (int) points[currentBatchIndex][i][4], (int) points[currentBatchIndex][i][5]);
                        Block block = startingLocation.clone().add(vector).getBlock();
                        cachedMaterial.put(vector, block.getType());
                        block.setType(paletteBlock.getMaterial());
                        block.setBlockData(Bukkit.createBlockData(paletteBlock.getMaterial()));
                    }
                    if (faces != null)
                        //Place the blocks using the face information.
                        //This is process after the vertex as it a bit less relevant.
                        for (int i = 0; i < faces[currentBatchIndex].length; i++) {
                            Vector vertex1 = getVectorFromPoints(faces[currentBatchIndex][i][0]);
                            Vector vertex2 = getVectorFromPoints(faces[currentBatchIndex][i][1]);
                            Vector vertex3 = getVectorFromPoints(faces[currentBatchIndex][i][2]);
                            for (int j = 0; j <= FACE_PRECISION; j++) {
                                for (int k = 0; k <= FACE_PRECISION - j; k++) {
                                    int l = FACE_PRECISION - j - k;
                                    Vector vector = vertex1.clone().multiply(l)
                                            .add(vertex2.clone().multiply(k))
                                            .add(vertex3.clone().multiply(j));
                                    vector = vector.multiply(1.0 / FACE_PRECISION);
                                    vector = new Vector((int) vector.getX(), (int) vector.getY(), (int) vector.getZ());
                                    if (cachedMaterial.containsKey(vector))
                                        continue;
                                    Vector color = getColorFromPoints(faces[currentBatchIndex][i][0]).clone().multiply(l)
                                            .add(getColorFromPoints(faces[currentBatchIndex][i][1]).clone().multiply(k))
                                            .add(getColorFromPoints(faces[currentBatchIndex][i][2]).clone().multiply(j));
                                    color = color.multiply(1.0 / FACE_PRECISION);
                                    PaletteBlock paletteBlock = AIBuilds.plugin.paletteManager.getPaletteBlock(
                                            (int) color.getX(), (int) color.getY(), (int) color.getZ());
                                    Block block = startingLocation.clone().add(vector).getBlock();
                                    cachedMaterial.put(vector, block.getType());
                                    block.setType(paletteBlock.getMaterial());
                                    block.setBlockData(Bukkit.createBlockData(paletteBlock.getMaterial()));
                                }
                            }
                        }
                }
        );

    }
}
