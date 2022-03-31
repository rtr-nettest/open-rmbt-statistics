package at.rtr.rmbt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface FileService {

    File openFile(String path);

    FileInputStream getFileInputStream(File file) throws FileNotFoundException;
}
