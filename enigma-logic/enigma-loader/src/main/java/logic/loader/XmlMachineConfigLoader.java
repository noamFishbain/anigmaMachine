package logic.loader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.schema.generated.*;
import logic.exceptions.EnigmaException;
import logic.loader.converters.XmlDtoConverter;
import logic.loader.validation.XmlValidationRules;
import logic.machine.Machine;
import logic.loader.dto.MachineDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/** Validates and loads the Enigma Machine configuration from an XML file.*/
public class XmlMachineConfigLoader implements MachineConfigLoader {
    private final XmlValidationRules validator; // Responsible for XML validation logic
    private final XmlDtoConverter converter;   // Responsible for converting JAXB objects to DTOs

    public XmlMachineConfigLoader() {
        this.validator = new XmlValidationRules();
        this.converter = new XmlDtoConverter();
    }

    // Loads the machine configuration from the specified XML file path
    @Override
    public Machine load(String filePath) throws Exception {
        // Validation: Check if file exists and has .xml extension
        File file = new File(filePath);
        if (!file.exists())
            throw new EnigmaException(EnigmaException.ErrorCode.
                    FILE_NOT_FOUND,
                    filePath);
        if (!filePath.endsWith(".xml"))
            throw new EnigmaException(EnigmaException.ErrorCode.
                    FILE_NOT_XML_TYPE,
                    filePath);

        // JAXB Unmarshalling: Convert XML file to auto-generated Java objects
        BTEEnigma bteEnigma = deserializeFromXML(new FileInputStream(file));

        // Logic Validation: Check against exercise rules (e.g., even ABC length)
        validator.validateMachineSpecs(bteEnigma);

        // Object Conversion: Convert JAXB objects to Domain objects (Machine, Rotor, etc.)
        return converter.createMachineFromBTE(bteEnigma);
    }

    // Unmarshals the XML input stream into the auto-generated JAXB classes
    private BTEEnigma deserializeFromXML(InputStream in) throws JAXBException {
        // Ensure the context path matches the package of your generated classes
        JAXBContext jc = JAXBContext.newInstance("jaxb.schema.generated");
        Unmarshaller u = jc.createUnmarshaller();
        return (BTEEnigma) u.unmarshal(in);
    }

    // Loads directly from an InputStream (uploaded file)
    public Machine load(InputStream inputStream) throws Exception {
        // JAXB Unmarshalling: Convert stream to auto-generated Java objects
        BTEEnigma bteEnigma = deserializeFromXML(inputStream);

        // Logic Validation
        validator.validateMachineSpecs(bteEnigma);

        // Object Conversion: Convert JAXB objects to Domain objects
        return converter.createMachineFromBTE(bteEnigma);
    }

    // Add this method to XmlMachineConfigLoader.java
    public MachineDescriptor loadDescriptor(InputStream inputStream) throws Exception {
        // 1. Deserialize (uses your existing private method)
        // Note: Since deserializeFromXML is private, make sure this method is in the same class
        BTEEnigma bteEnigma = deserializeFromXML(inputStream);

        // 2. Validate (uses your existing validator)
        validator.validateMachineSpecs(bteEnigma);

        // 3. Convert to DTO instead of full Machine logic object
        return converter.convertToDescriptor(bteEnigma);
    }
}