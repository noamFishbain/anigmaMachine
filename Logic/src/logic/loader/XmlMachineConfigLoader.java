package logic.loader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generated.*;
import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;
import logic.loader.dto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/** Validates and loads the Enigma Machine configuration from an XML file.*/
public class XmlMachineConfigLoader implements MachineConfigLoader {

    // Loads the machine configuration from the specified XML file path.
    @Override
    public Machine load(String filePath) throws Exception {
        // Validation: Check if file exists and has .xml extension
        File file = new File(filePath);
        if (!file.exists())
            throw new Exception("File not found: " + filePath);
        if (!filePath.endsWith(".xml"))
            throw new Exception("File must be an XML file.");

        // JAXB Unmarshalling: Convert XML file to auto-generated Java objects
        BTEEnigma bteEnigma = deserializeFromXML(new FileInputStream(file));

        // Logic Validation: Check against exercise rules (e.g., even ABC length)
        validateMachineSpecs(bteEnigma);

        // Object Conversion: Convert JAXB objects to Domain objects (Machine, Rotor, etc.)
        return createMachineFromBTE(bteEnigma);
    }

    // Unmarshals the XML input stream into the auto-generated JAXB classes
    private BTEEnigma deserializeFromXML(InputStream in) throws JAXBException {
        // Ensure the context path matches the package of your generated classes
        JAXBContext jc = JAXBContext.newInstance("jaxb.schema.generated");
        Unmarshaller u = jc.createUnmarshaller();
        return (BTEEnigma) u.unmarshal(in);
    }

    // Validates the logical integrity of the loaded XML data
    private void validateMachineSpecs(BTEEnigma enigma) throws Exception {
        String abc = enigma.getABC().trim();

        validateABC(abc);
        validateRotors(enigma.getBTERotors().getBTERotor(), abc); // Updated to pass ABC string
        validateReflectors(enigma.getBTEReflectors().getBTEReflector());
    }

    // Validation: alphabet length
    private void validateABC(String abc) throws Exception {
        if (abc.length() % 2 != 0) {
            throw new Exception("ABC size must be even. Current size: " + abc.length());
        }
    }

    // Validation: Rotors (Count, Sequence, Mapping size, Logic)
    private void validateRotors(List<BTERotor> rotors, String abc) throws Exception {
        int expectedMappingSize = abc.length();

        // Check if there are enough rotors defined (Minimum 3 expected)
        if (rotors.size() < 3) {
            throw new Exception("Not enough rotors defined. Machine must define at least 3 rotors.");
        }

        // Check sequential IDs (1, 2, 3...)
        List<Integer> ids = rotors.stream().map(BTERotor::getId).sorted().collect(Collectors.toList());
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) != (i + 1)) {
                throw new Exception("Rotor IDs must be unique and sequential (1 to N).");
            }
        }

        // Check mapping size matches ABC and logical validity (no duplicates)
        for (BTERotor rotor : rotors) {
            if (rotor.getBTEPositioning().size() != expectedMappingSize) {
                throw new Exception("Rotor ID " + rotor.getId() + " positioning count does not match ABC size.");
            }

            // Validate mapping logic (Duplicate keys in Right/Left)
            validateRotorMappingLogic(rotor, abc);
        }
    }

    // Validates that a single rotor has a 1-to-1 mapping for every character in the ABC
    private void validateRotorMappingLogic(BTERotor rotor, String abc) throws Exception {
        Set<String> rightSideChars = new HashSet<>();
        Set<String> leftSideChars = new HashSet<>();

        for (BTEPositioning pos : rotor.getBTEPositioning()) {
            String right = pos.getRight().toUpperCase();
            String left = pos.getLeft().toUpperCase();

            // 1. Check if characters are valid (exist in ABC)
            if (abc.indexOf(right) == -1 || abc.indexOf(left) == -1) {
                throw new Exception("Rotor " + rotor.getId() + " contains invalid characters not in ABC: " + right + ", " + left);
            }

            // 2. Check for duplicates in the RIGHT column (Source)
            // If "A" appears twice in 'right', it means 'A' maps to two different things -> Invalid.
            if (rightSideChars.contains(right)) {
                throw new Exception("Rotor " + rotor.getId() + " maps source char '" + right + "' more than once (Duplicate Mapping)!");
            }
            rightSideChars.add(right);

            // 3. Check for duplicates in the LEFT column (Target)
            // In Enigma, rotors must be bijective (1-to-1), so duplicates here are also invalid.
            if (leftSideChars.contains(left)) {
                throw new Exception("Rotor " + rotor.getId() + " maps to target char '" + left + "' more than once!");
            }
            leftSideChars.add(left);
        }
    }

    // Validation: Reflectors (sequential soman IDs)
    private void validateReflectors(List<BTEReflector> reflectors) throws Exception {
        // Check Sequential Roman IDs
        List<Integer> ids = reflectors.stream()
                .map(r -> convertRomanToInt(r.getId()))
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) != (i + 1)) {
                throw new Exception("Reflector IDs must be unique and sequential (I to N).");
            }
        }
    }

    // Converts the raw BTEEnigma object into a MachineDescriptor and initializes the MachineImpl
    private Machine createMachineFromBTE(BTEEnigma bteEnigma) {
        String abc = bteEnigma.getABC().trim();

        // Create rotor descriptors
        List<RotorDescriptor> rotorDescriptors = getRotorDescriptors(bteEnigma, abc);

        // Create reflector descriptors
        List<ReflectorDescriptor> reflectorDescriptors = new ArrayList<>();

        for (BTEReflector bteRef : bteEnigma.getBTEReflectors().getBTEReflector()) {
            reflectorDescriptors.add(createReflectorDescriptor(bteRef));
        }

        // Get required rotors count
        int requiredRotorsCount = 3;;

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

    private RotorDescriptor createSingleRotorDescriptor(BTERotor bteRotor, String abc) {
        int id = bteRotor.getId();
        int notch = bteRotor.getNotch() ;

        // CHANGED: Use the new calculation method for [ABC][2] array
        int[][] mapping = calculateLocationMapping(bteRotor.getBTEPositioning(), abc);

        // Note: Assuming RotorDescriptor constructor now accepts int[][] mapping
        return new RotorDescriptor(id, mapping, notch);
    }

    // New Method: Creates a normalized table where each cell represents a character from ABC
    // Cell [i][0] holds the row index where char 'i' appears in the RIGHT column
    // Cell [i][1] holds the row index where char 'i' appears in the LEFT column
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

    private ReflectorDescriptor createReflectorDescriptor(BTEReflector bteRef) {
        List<int[]> pairs = new ArrayList<>();
        for (BTEReflect r : bteRef.getBTEReflect()) {
            // XML uses 1-based index, we convert to 0-based
            pairs.add(new int[]{r.getInput() - 1, r.getOutput() - 1});
        }
        return new ReflectorDescriptor(bteRef.getId(), pairs);
    }

    // Converts Roman numeral strings (I-V) to their integer representation
    private int convertRomanToInt(String roman) {
        switch (roman.toUpperCase()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            default: throw new IllegalArgumentException("Unknown reflector ID: " + roman);
        }
    }

    // ... (Your existing commented-out tests remain here)
}