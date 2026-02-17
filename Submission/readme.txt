# Enigma Machine Console Application - Project

This project implements a console-based simulation of the historical Enigma Machine.
The system is designed with a strict separation of concerns between the User Interface (UI) layer
and the Core Logic (Engine) layer, allowing clean architecture, modularity, and extensibility.

The application allows:
- Loading Enigma machine configurations from XML files
- Validating configurations according to Enigma rules
- Setting up the machine state
- Encrypting and decrypting messages
- Viewing machine specifications and history


1. Execution Instructions (Run Command & JARs)

The application is modular and built from two separate JAR files:

* ConsoleUI.jar: Contains the command-line interface logic (UI).
* Logic.jar: Contains the core business logic of the Enigma machine (Engine).

To run the application, ensure the JARs and the scripts are in the same directory and execute the appropriate file.

Execution Commands:
* run.bat (Windows):`java -cp ConsoleUI.jar;Logic.jar ui.Main`
* run.sh (Mac/Linux):`java -cp ConsoleUI.jar:Logic.jar ui.Main`

2. Project Source Code (GitHub Link)

The full source code for this project is available on GitHub at the following link:
https://github.com/noamFishbain/anigmaMachine

3. Development Decisions & Design Choices
* The project is divided into two independent modules: ConsoleUI and Logic.
The UI communicates with the logic layer only via interfaces and DTOs.
* EnigmaEngine serves as a facade that exposes a clean and minimal API to the UI layer.
* XML Parsing: JAXB was chosen for XML deserialization to ensure strong typing, schema validation,
and clear mapping between XML and Java objects.
* Validation Strategy:
Validation is split into:
Schema-level validation (XSD)
Logical validation using explicit validation rules
* Error Handling:
Custom runtime exceptions (EnigmaException) are used to signal fatal logic or configuration errors,
while the UI layer is responsible for user-friendly error reporting.

4. Architecture & Core Classes Documentation

A. Console UI Layer
* Main- Application entry point. Initializes the Enigma engine and starts the main console loop.
* ConsoleApp- Manages the overall application flow, menu navigation, and coordination between user actions and the engine.
* ConsoleMenu- Displays menus and available user actions and controls UI state transitions.
* ConsoleInputReader- Responsible for reading raw input from the console in a controlled manner.
* ConsoleInputCollector- Aggregates and validates sequences of user inputs before passing structured data to the engine.

B. Engine & Core Logic
* EnigmaEngine (Interface)- Defines the contract for all operations exposed to the UI, including loading configurations,
retrieving machine specifications, setting up the machine, processing text, and resetting state.
* EnigmaEngineImpl- Concrete implementation of the engine facade.
Manages the runtime state of the machine and delegates encryption tasks to the machine model.
* MachineSpecs- Immutable data object summarizing the current machine configuration and runtime status.
* CodeConfiguration- Represents a complete machine setup including rotor order, initial positions, and reflector selection.

C. Machine & Components
* Machine (Interface)- Defines the core behavior of an Enigma machine, including character processing and state transitions.
* MachineImpl- Implements the full encryption and decryption flow by coordinating keyboard, rotors, and reflector.
* Rotor (Interface)- Defines rotor behavior including character mapping and stepping logic.
* RotorImpl- Implements rotor wiring, rotation, and notch-based stepping behavior.
* Reflector (Interface)- Defines the reflector component behavior.
* ReflectorImpl- Implements fixed bidirectional character mappings.
* Keyboard (Interface)- Defines character-to-index and index-to-character mappings.
*KeyboardImpl- Concrete implementation of character-index translation used throughout the machine.

D. XML Loading, Validation & DTOs
* MachineConfigLoader (Interface)- Defines an abstraction for loading Enigma machine configurations.
* XmlMachineConfigLoader- Loads and validates machine configurations from XML files using JAXB and schema validation.
* XmlValidationRules- Applies logical validation rules beyond XSD validation.
* MachineDescriptor- Root DTO representing the complete machine configuration loaded from XML.
* RotorDescriptor- DTO representing a rotor definition extracted from XML.
* ReflectorDescriptor- DTO representing a reflector definition extracted from XML.
* MachineHistoryRecord- Records historical machine configurations and processed messages.
* XmlDtoConverter- Converts JAXB-generated XML objects into internal DTOs and domain objects.

E. Utilities & Validation
* InputParser- Parses and normalizes user input into structured data.
* CodeFormatter- Formats encoded and decoded messages for consistent console output.
* EnigmaCodeValidator- Validates machine configurations and encoded messages according to Enigma rules.

F. Error Handling
* EnigmaException- Custom runtime exception used to report fatal logic or configuration errors from the logic layer to the UI.

5. Development Decisions & Additional Notes for the Examiner

* XML Parsing Framework: JAXB was used for deserialization of the XML configuration files. This ensured robust type checking and automatic mapping of the XML schema (`Enigma-Ex1.xsd`) to Java objects.
* Data Transfer: All communication between the `ConsoleUI` and `Logic` is handled through interfaces and DTOs (e.g., `MachineSpecs`, `MachineHistoryRecord`) to maintain low coupling.
* Error Handling: Custom exceptions (`EnigmaException`) are used to clearly signal fatal logic errors back to the UI, which then handles the user-friendly reporting.
