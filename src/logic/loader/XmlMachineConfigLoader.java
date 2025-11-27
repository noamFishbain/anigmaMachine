package logic.loader;

import logic.loader.dto.MachineDescriptor;

import java.io.File;

public class XmlMachineConfigLoader implements  MachineConfigLoader {

    @Override
    public MachineDescriptor load(String filePath) throws Exception {
        validatePath(filePath);
        // JAXB later to transfer the data from the XML file
        return null;
    }
    private void validatePath(String path) throws Exception {
        if (path == null || path.isBlank())
            throw new Exception("Path cannot be empty.");
        if (!path.endsWith(".xml"))
            throw new Exception("File must be .xml");
        File file = new File(path);
        if (!file.exists())
            throw new Exception("File not found.");
    }
}
