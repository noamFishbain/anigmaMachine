package logic.loader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generated.*;
import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;
import logic.machine.components.*;
import logic.loader.dto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class XmlMachineConfigLoader implements MachineConfigLoader {

    @Override
    public Machine load(String filePath) throws Exception {
        // 1. Validation: Check if file exists and has .xml extension
        File file = new File(filePath);
        if (!file.exists()) throw new Exception("File not found: " + filePath);
        if (!filePath.endsWith(".xml")) throw new Exception("File must be an XML file.");

        // 2. JAXB Unmarshalling: Convert XML file to auto-generated Java objects
        BTEEnigma bteEnigma = deserializeFromXML(new FileInputStream(file));

        // 3. Logic Validation: Check against exercise rules (e.g., even ABC length)
        validateMachineSpecs(bteEnigma);

        // 4. Object Conversion: Convert JAXB objects to Domain objects (Machine, Rotor, etc.)
        return createMachineFromBTE(bteEnigma);
    }

    private BTEEnigma deserializeFromXML(InputStream in) throws JAXBException {
        // Ensure the context path matches the package of your generated classes
        JAXBContext jc = JAXBContext.newInstance("jaxb.schema.generated");
        Unmarshaller u = jc.createUnmarshaller();
        return (BTEEnigma) u.unmarshal(in);
    }

    private void validateMachineSpecs(BTEEnigma enigma) throws Exception {
        String abc = enigma.getABC().trim();

        // Retrieve necessary lists for validation
        List<BTERotor> bteRotors = enigma.getBTERotors().getBTERotor();
        List<BTEReflector> bteReflectors = enigma.getBTEReflectors().getBTEReflector();

        // Check if ABC length is even
        if (abc.length() % 2 != 0) {
            throw new Exception("ABC size must be even. Current size: " + abc.length());
        }

        // Check if there are enough rotors defined (Minimum 3 expected)
        if (enigma.getBTERotors().getBTERotor().size() < 3) {
            throw new Exception("Not enough rotors defined. Machine must define at least 3 rotors.");
        }

        // Checks that rotor IDs are unique and form a running sequence (1 to N)
        List<Integer> rotorIDs = bteRotors.stream()
                .map(BTERotor::getId)
                .sorted()
                .toList();

        for (int i = 0; i < rotorIDs.size(); i++) {
            if (rotorIDs.get(i) != (i + 1)) {
                throw new Exception("Rotor IDs must be unique and form a running sequence (1 to N). Missing ID or hole found.");
            }
        }

        // Checks that rotor's positioning list matches ABC size (Mapping integrity check)
        int expectedSize = abc.length();
        for (BTERotor bteRotor : bteRotors) {
            if (bteRotor.getBTEPositioning().size() != expectedSize) {
                throw new Exception("Rotor ID " + bteRotor.getId() + " positioning count does not match ABC size (" + expectedSize + ").");
            }
        }

        // Checks that reflector IDs are unique and form a running Roman sequence (I to N)
        List<Integer> reflectorDecimalIDs = bteReflectors.stream()
                .map(BTEReflector::getId) // Extract the Roman ID (String)
                .map(this::convertRomanToInt) // Convert the String to int
                .sorted()
                .toList();

        for (int i = 0; i < reflectorDecimalIDs.size(); i++) {
            if (reflectorDecimalIDs.get(i) != (i + 1)) {
                throw new Exception("Reflector IDs must be unique and form a running Roman sequence (I to N). Missing ID or hole found.");
            }
        }
    }

    private Machine createMachineFromBTE(BTEEnigma bteEnigma) {
        String abc = bteEnigma.getABC().trim();
        List<RotorDescriptor> rotorDescriptors = getRotorDescriptors(bteEnigma, abc);

        // --- 2. Create Reflector Descriptors List ---
        List<ReflectorDescriptor> reflectorDescriptors = new ArrayList<>();

        for (BTEReflector bteReflector : bteEnigma.getBTEReflectors().getBTEReflector()) {
            String id = bteReflector.getId(); // Keep as String (Roman) for the descriptor

            List<int[]> pairs = new ArrayList<>();

            for (BTEReflect reflect : bteReflector.getBTEReflect()) {
                int inputIdx = reflect.getInput() - 1;
                int outputIdx = reflect.getOutput() - 1;
                pairs.add(new int[]{inputIdx, outputIdx});
            }

            // Create ReflectorDescriptor
            ReflectorDescriptor descriptor = new ReflectorDescriptor();
            descriptor.setId(id); // "I", "II", etc.
            descriptor.setPairs(pairs);

            reflectorDescriptors.add(descriptor);
        }

        // --- 3. Set data to MachineDescriptor ---
        MachineDescriptor machineDescriptor = new MachineDescriptor();
        machineDescriptor.setABC(abc);
        machineDescriptor.setRotors(rotorDescriptors);
        machineDescriptor.setReflectors(reflectorDescriptors);

        // --- 4. Return new Machine ---
        // MachineImpl constructor will now take the descriptor and create the actual Logic components
        return new MachineImpl(machineDescriptor);
    }

    private static List<RotorDescriptor> getRotorDescriptors(BTEEnigma bteEnigma, String abc) {
        int abcLength =  abc.length();

        // Create Rotor Descriptors List
        List<RotorDescriptor> rotorDescriptors = new ArrayList<>();

        for (BTERotor bteRotor : bteEnigma.getBTERotors().getBTERotor()) {
            int id = bteRotor.getId();
            int notch = bteRotor.getNotch();

            // Calculate mapping array
            List<Integer> forwardMapping = new ArrayList<>(Collections.nCopies(abcLength, 0));
            for (BTEPositioning pos : bteRotor.getBTEPositioning()) {
                char rightChar = pos.getRight().charAt(0);
                char leftChar = pos.getLeft().charAt(0);
                int inputIndex = abc.indexOf(rightChar);
                int outputIndex = abc.indexOf(leftChar);
                forwardMapping.set(inputIndex, outputIndex);
            }

            // Create RotorDescriptor
            // Assuming RotorDescriptor has a constructor: (int id, int notch, int[] mapping)
            // If not, use setters:
            RotorDescriptor descriptor = new RotorDescriptor();
            descriptor.setId(id);
            descriptor.setNotchPosition(notch);
            descriptor.setMapping(forwardMapping); // You might need to add this method to RotorDescriptor

            rotorDescriptors.add(descriptor);
        }
        return rotorDescriptors;
    }

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
}