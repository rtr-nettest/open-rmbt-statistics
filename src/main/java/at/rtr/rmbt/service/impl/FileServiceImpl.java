package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.service.FileService;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public File openFile(String path) {
        return new File(path);
    }
}
