package logic.loader.dto;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a static definition of a Rotor, as loaded from the configuration file.
 * This object is immutable.
 * Updated to hold the normalized position table ([ABC][2]) instead of a simple mapping list.
 */
public class RotorDescriptor implements Serializable {

    private final int id;
    private final int notchPosition;

    // Holds the normalized [ABC_Size][2] array:
    // mapping[i][0] = Row index of char 'i' in the Right column
    // mapping[i][1] = Row index of char 'i' in the Left column
    private final int[][] mapping;

    public RotorDescriptor(int id, int[][] mapping, int notchPosition) {
        this.id = id;
        this.notchPosition = notchPosition;

        // Save the array (no need for deep copy if we trust the Loader,
        // but for safety/immutability we could clone it. Here we just assign it.)
        this.mapping = mapping;
    }

    public int getId() {
        return id;
    }

    public int getNotchPosition() {
        return notchPosition;
    }

    // Returns the [ABC][2] position table
    public int[][] getMapping() {
        return mapping;
    }

    @Override
    public String toString() {
        return "RotorDescriptor{" +
                "id=" + id +
                ", notch=" + notchPosition +
                ", mapping=" + Arrays.deepToString(mapping) +
                '}';
    }
}