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
        validateRotors(enigma.getBTERotors().getBTERotor(), abc.length());
        validateReflectors(enigma.getBTEReflectors().getBTEReflector());
    }

    // Validation: alphabet length
    private void validateABC(String abc) throws Exception {
        if (abc.length() % 2 != 0) {
            throw new Exception("ABC size must be even. Current size: " + abc.length());
        }
    }

    // Validation: Rotors (Count, Sequence, Mapping size)
    private void validateRotors(List<BTERotor> rotors, int expectedMappingSize) throws Exception {
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

        // Check mapping size matches ABC
        for (BTERotor rotor : rotors) {
            if (rotor.getBTEPositioning().size() != expectedMappingSize) {
                throw new Exception("Rotor ID " + rotor.getId() + " positioning count does not match ABC size.");
            }
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

    /// TEST FOR LOADING XML
//    public static void main(String[] args) {
//        try {
//            String testFile = "/Users/noamfishbain/Documents/anigmaMachine/resources/ex1-sanity-small.xml"; // וודא שהנתיב נכון (אולי צריך path מלא)
//            System.out.println("--- Starting XML Load Test for: " + testFile + " ---");
//
//            XmlMachineConfigLoader loader = new XmlMachineConfigLoader();
//
//            // 1. Unmarshal directly (simulate step 2 of load)
//            File file = new File(testFile);
//            BTEEnigma bteEnigma = loader.deserializeFromXML(new FileInputStream(file));
//            System.out.println("Status: JAXB Unmarshalling successful.");
//
//            // 2. Convert to Descriptor (simulate step 4 of load)
//            // שים לב: אנחנו משכפלים פה חלק מהלוגיקה של createMachineFromBTE כדי להציץ בנתונים
//            // כי הפונקציה המקורית מחזירה MachineImpl ואנחנו רוצים לראות את ה-Descriptor
//
//            String abc = bteEnigma.getABC().trim();
//            System.out.println("ABC Found: [" + abc + "]");
//            System.out.println("ABC Length: " + abc.length());
//
//            List<RotorDescriptor> rotors = getRotorDescriptors(bteEnigma, abc);
//            System.out.println("\n--- Rotors Loaded: " + rotors.size() + " ---");
//
//            for (RotorDescriptor r : rotors) {
//                System.out.println("Rotor ID: " + r.getId());
//                System.out.println("  Notch: " + r.getNotchPosition());
//
//                // בדיקת המיפוי של האות הראשונה (A)
//                int indexA = abc.indexOf('A'); // אמור להיות 0
//                if (indexA != -1) {
//                    int mappedIndex = r.getMapping().get(indexA);
//                    char mappedChar = abc.charAt(mappedIndex);
//                    System.out.println("  Mapping check: 'A' maps to -> '" + mappedChar + "' (Index " + mappedIndex + ")");
//                }
//            }
//
//            System.out.println("\n--- Reflectors Loaded ---");
//            for (BTEReflector bteRef : bteEnigma.getBTEReflectors().getBTEReflector()) {
//                System.out.println("Reflector ID: " + bteRef.getId());
//                for (BTEReflect pair : bteRef.getBTEReflect()) {
//                    System.out.println("  Pair: " + pair.getInput() + " <-> " + pair.getOutput());
//                }
//            }
//
//            System.out.println("\n--- Test Finished Successfully ---");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /// TEST FOR MAKING A MACHINE FROM THE XML

//    public static void main(String[] args) {
//        try {
//            System.out.println("--- Starting Step 2: Machine Object Logic Test ---");
//            XmlMachineConfigLoader loader = new XmlMachineConfigLoader();
//
//            // 1. Load the Machine (This creates the MachineImpl object)
//            Machine machine = loader.load("/Users/noamfishbain/Documents/anigmaMachine/resources/ex1-sanity-small.xml");
//            System.out.println("Machine object created successfully.");
//
//            // בדיקת כמות כוללת
//            if (machine.getAllAvailableRotors().size() != 3) {
//                System.out.println("ERROR: Expected 3 rotors, found " + machine.getAllAvailableRotors().size());
//            }
//
//            // --- בדיקת רוטור מס' 1 ---
//            // XML: id="1" notch="4" (Right A -> Left F)
//            validateSingleRotor(machine, 1, 3, 5);
//
//            // --- בדיקת רוטור מס' 2 ---
//            // XML: id="2" notch="1" (Right A -> Left E)
//            // שים לב: ב-XML הוא מופיע אחרון, אבל ה-ID שלו הוא 2.
//            validateSingleRotor(machine, 2, 0, 4);
//
//            // --- בדיקת רוטור מס' 3 ---
//            // XML: id="3" notch="2" (Right A -> Left A)
//            validateSingleRotor(machine, 3, 1, 0);
//
//            System.out.println("\n--- Step 2 Finished ---");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // פונקציית עזר לבדיקת רוטור בודד
//    private static void validateSingleRotor(Machine machine, int id, int expectedNotchIndex, int expectedMappingForZero) {
//        System.out.println("\nChecking Rotor ID: " + id + "...");
//
//        Rotor r = machine.getAllAvailableRotors().get(id);
//        if (r == null) {
//            System.out.println("  [ERROR] Rotor ID " + id + " not found in memory!");
//            return;
//        }
//
//        // 1. בדיקת ID
//        if (r.getId() != id) {
//            System.out.println("  [ERROR] Internal ID mismatch. Key=" + id + ", Value.id=" + r.getId());
//        }
//
//        // 2. בדיקת Notch (המרת XML לאינדקס)
//        if (r.getNotch() != expectedNotchIndex) {
//            System.out.println("  [FAIL] Notch: " + r.getNotch() + " (Expected: " + expectedNotchIndex + ")");
//        } else {
//            System.out.println("  [OK] Notch: " + r.getNotch());
//        }
//
//        // 3. בדיקת מיפוי (עבור אינדקס 0 בלבד)
//        int actualMap = r.mapForward(0);
//        if (actualMap != expectedMappingForZero) {
//            System.out.println("  [FAIL] Map(0): " + actualMap + " (Expected: " + expectedMappingForZero + ")");
//        } else {
//            System.out.println("  [OK] Map(0) -> " + actualMap);
//        }
//    }

    /// TEST FOR GETTING MANUAL CONFIG FROM THE USER

//    public static void main(String[] args) {
//        try {
//            System.out.println("--- Starting Step 2.1: Manual Configuration Input Logic ---");
//
//            // 1. Initialize Engine and Load XML
//            // We use the Engine now, because the validation logic resides there
//            logic.engine.EnigmaEngineImpl engine = new logic.engine.EnigmaEngineImpl();
//            engine.loadMachineFromXml("/Users/noamfishbain/Documents/anigmaMachine/resources/ex1-sanity-small.xml");
//
//            System.out.println("Machine loaded successfully.");
//
//            // ==========================================
//            // TEST A: Valid Configuration (Happy Path)
//            // ==========================================
//            // Requirement:
//            // Rotors Input: "3,2,1" (Left-to-Right entry) -> Means Rotor 3 is Left, Rotor 1 is Right.
//            // Positions Input: "CCC" (Left-to-Right entry) -> First char 'C' is for RIGHTMOST rotor (ID 1).
//            // Reflector Input: 1 (-> "I")
//
//            System.out.println("\n[Test A] Trying Valid Configuration: <3,2,1> <CCC> <1>...");
//            try {
//                // Note: setManualCode returns the formatted string code
//                String result = engine.setManualCode("3,2,1", "CCC", 1);
//                System.out.println("  Result: " + result);
//
//                // Expected internal state check via specs
//                // If logic is correct:
//                // Rotor 1 (Right) gets 'C', Rotor 2 (Mid) gets 'C', Rotor 3 (Left) gets 'C'.
//                // If your engine reverses the list correctly, formatConfiguration should show <3,2,1>.
//                if (result.contains("<3,2,1>") && result.contains("<I>")) {
//                    System.out.println("  [PASS] Valid Code Accepted.");
//                } else {
//                    System.out.println("  [FAIL] Unexpected formatting. Got: " + result);
//                }
//            } catch (Exception e) {
//                System.out.println("  [FAIL] valid code threw exception: " + e.getMessage());
//                e.printStackTrace();
//            }
//
//            // ==========================================
//            // TEST B: Invalid Inputs (Validation Logic)
//            // ==========================================
//            System.out.println("\n[Test B] Testing Validations...");
//
//            // 1. Duplicate Rotors
//            checkError(engine, "1,1,2", "ABC", 1, "Duplicate Rotors");
//
//            // 2. Missing Rotor ID (ID 5 doesn't exist in sanity-small)
//            checkError(engine, "1,2,5", "ABC", 1, "Non-existent Rotor ID");
//
//            // 3. Wrong Amount of Rotors (Need 3)
//            checkError(engine, "1,2", "ABC", 1, "Too few Rotors");
//
//            // 4. Invalid Position Char ( '!' is not in ABCDEF)
//            checkError(engine, "3,2,1", "AB!", 1, "Invalid Alphabet Char");
//
//            // 5. Mismatched Position Length (2 chars for 3 rotors)
//            checkError(engine, "3,2,1", "AB", 1, "Positions length mismatch");
//
//            // 6. Invalid Reflector ID (Range 1-5)
//            checkError(engine, "3,2,1", "ABC", 9, "Invalid Reflector ID");
//
//            System.out.println("\n--- Step 2.1 Finished ---");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Helper to print PASS/FAIL for error checking
//    private static void checkError(logic.engine.EnigmaEngineImpl engine, String rotors, String pos, int ref, String testName) {
//        System.out.print("  Checking " + testName + " (" + rotors + ", " + pos + ")... ");
//        try {
//            engine.setManualCode(rotors, pos, ref);
//            System.out.println("[FAIL] Should have thrown exception but didn't.");
//        } catch (Exception e) {
//            System.out.println("[PASS] Caught expected error: " + e.getMessage());
//        }
//}


    /// TEST FOR ENCRYPTING SINGLE CHAR

    /*public static void main(String[] args) {
        try {
            System.out.println("--- Testing 'Step-After-Process' Theory ---");

            // 1. Load Engine
            logic.engine.EnigmaEngineImpl engine = new logic.engine.EnigmaEngineImpl();
            engine.loadMachineFromXml("/Users/noamfishbain/Documents/anigmaMachine/resources/ex1-sanity-small.xml");
            engine.setDebugMode(true);

            // 2. Configure: Rotors 3,2,1 | Positions CCC | Reflector I
            System.out.println("Setting Config: <3,2,1><CCC><I>");
            engine.setManualCode("3,2,1", "CCC", 1);

            // 3. Process 'A'
            String input = "AABBCCDDEEFF";
            System.out.println("\n--- Processing Input: " + input + " ---");
            String output = engine.process(input);

            System.out.println("\n--- Result ---");
            System.out.println("Input:  " + input);
            System.out.println("Output: " + output);

            // 4. Verification
            if ("F".equals(output)) {
                System.out.println("\n[SUCCESS] !!! Got 'F' !!!");
                System.out.println("The theory is correct: The machine encrypts first, then steps.");
            } else {
                System.out.println("\n[FAIL] Still got '" + output + "' (Expected 'F')");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}