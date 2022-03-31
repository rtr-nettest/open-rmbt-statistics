package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.service.FileService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public File openFile(String path) {
        return new File(path);
    }

    @Override
    public FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
