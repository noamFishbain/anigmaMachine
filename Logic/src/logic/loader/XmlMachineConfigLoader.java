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
            List<BTEPositioning> positions = bteRotor.getBTEPositioning();

            // אנו רצים עם אינדקס i שרץ מ-0 עד 25 (גודל ה-ABC)
            for (int i = 0; i < positions.size(); i++) {
                BTEPositioning pos = positions.get(i);

                // אנחנו מסתכלים על האות בעמודה השמאלית
                char leftChar = pos.getLeft().charAt(0);

                // בודקים מה האינדקס של האות הזו ב-ABC (למשל A->0, B->1)
                // זה קובע *איפה* נכתוב במערך התוצאה
                int charIndexInAbc = abc.indexOf(leftChar);

                // אנחנו שמים שם את ה-i (האינדקס של השורה הנוכחית)
                // אם A הופיעה בשורה 4 (אינדקס 4), אז במיקום 0 במערך יהיה כתוב 4
                forwardMapping.set(charIndexInAbc, i);
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

    public static void main(String[] args) {
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
    }
}