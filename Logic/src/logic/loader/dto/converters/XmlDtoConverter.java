package logic.loader.dto.converters;

import jaxb.schema.generated.BTEEnigma;
import jaxb.schema.generated.BTEPositioning;
import jaxb.schema.generated.BTEReflect;
import jaxb.schema.generated.BTEReflector;
import jaxb.schema.generated.BTERotor;
import logic.loader.dto.MachineDescriptor;
import logic.loader.dto.ReflectorDescriptor;
import logic.loader.dto.RotorDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;

import java.util.ArrayList;
import java.util.List;

/** This class is responsible for converting raw JAXB objects loaded from XML
 * into clean, decoupled DTOs (Data Transfer Objects) used by the Engine layer
 */
public class XmlDtoConverter {

    // Converts the raw BTEEnigma object into a MachineDescriptor and initializes the MachineImpl
    public Machine createMachineFromBTE(BTEEnigma bteEnigma) {
        String abc = bteEnigma.getABC().trim();

        // Create rotor descriptors
        List<RotorDescriptor> rotorDescriptors = getRotorDescriptors(bteEnigma, abc);

        // Create reflector descriptors
        List<ReflectorDescriptor> reflectorDescriptors = new ArrayList<>();

        for (BTEReflector bteRef : bteEnigma.getBTEReflectors().getBTEReflector()) {
            reflectorDescriptors.add(createReflectorDescriptor(bteRef));
        }

        // Get required rotors count
        int requiredRotorsCount = 3;

        // Build the final Descriptor
        MachineDescriptor descriptor = new MachineDescriptor(
                abc,
                rotorDescriptors,
                reflectorDescriptors,
                requiredRotorsCount
        );

        return new MachineImpl(descriptor);
    }

    // Helper method to parse BTE rotors into RotorDescriptor objects.
    private List<RotorDescriptor> getRotorDescriptors(BTEEnigma bteEnigma, String abc) {
        List<RotorDescriptor> result = new ArrayList<>();
        for (BTERotor bteRotor : bteEnigma.getBTERotors().getBTERotor()) {
            result.add(createSingleRotorDescriptor(bteRotor, abc));
        }
        return result;
    }

    // Converts a single BTERotor JAXB object into a RotorDescriptor DTO.
    private RotorDescriptor createSingleRotorDescriptor(BTERotor bteRotor, String abc) {
        int id = bteRotor.getId();
        int notch = bteRotor.getNotch();

        // Use the new calculation method for [ABC][2] array
        int[][] mapping = calculateLocationMapping(bteRotor.getBTEPositioning(), abc);

        // Assuming RotorDescriptor constructor now accepts int[][] mapping
        return new RotorDescriptor(id, mapping, notch);
    }

    // Creates a normalized position table based on the XML's Left/Right mapping
    // The resulting array allows the Rotor component to map forward and backward quickly.
    // Cell [i][0] holds the row index where char 'i' appears in the RIGHT column (forward path)
    // Cell [i][1] holds the row index where char 'i' appears in the LEFT column (backward path)
    private int[][] calculateLocationMapping(List<BTEPositioning> positions, String abc) {
        int length = abc.length();
        int[][] mapping = new int[length][2];

        for (int i = 0; i < positions.size(); i++) {
            BTEPositioning pos = positions.get(i);

            char rightChar = pos.getRight().charAt(0);
            char leftChar = pos.getLeft().charAt(0);

            int rightIndex = abc.indexOf(rightChar);
            int leftIndex = abc.indexOf(leftChar);

            // Store the row index 'i' in the corresponding character's cell
            mapping[rightIndex][0] = i; // Right column position for this char
            mapping[leftIndex][1] = i;  // Left column position for this char
        }
        return mapping;
    }

    // Converts a single BTEReflector JAXB object into a ReflectorDescriptor DTO
    private ReflectorDescriptor createReflectorDescriptor(BTEReflector bteRef) {
        List<int[]> pairs = new ArrayList<>();
        for (BTEReflect r : bteRef.getBTEReflect()) {
            // XML uses 1-based index, we convert to 0-based
            pairs.add(new int[]{r.getInput() - 1, r.getOutput() - 1});
        }
        return new ReflectorDescriptor(bteRef.getId(), pairs);
    }
}