package fr.phoenix.aibuilds.utils;

import org.bukkit.entity.Player;

public class Utils {

    public static double[][] stringToMatrix(String str) {
        String[] rowArr = str.split(";");
        double[][] matrix = new double[rowArr.length][];
        for (int i = 0; i < rowArr.length; i++) {
            String[] colArr = rowArr[i].split(",");
            matrix[i] = new double[colArr.length];
            for (int j = 0; j < colArr.length; j++) {
                matrix[i][j] = Double.parseDouble(colArr[j]);
            }
        }
        return matrix;
    }

    public static String formatTime(long time) {
        if (time / (1000 * 3600) > 1) {
            //{hours} hours, {minutes} minutes format
            //{day} day, {hour} hours format.
            int hour = (int) (time / (1000 * 3600));
            int minute = (int) (time / (1000 * 60) - 60 * hour);
            return hour + "h, " + minute + "min";
        } else {
            int minute = (int) (time / (1000 * 60));
            int second = (int) (time / 1000 - 60 * minute);
            return minute + "min " + second + "s";
        }

    }
}
