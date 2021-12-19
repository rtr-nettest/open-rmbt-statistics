package at.rtr.rmbt.service;

import at.rtr.rmbt.dto.ImageGenerateDto;

public interface ImageExportService {

    byte[] generateImage(ImageGenerateDto imageGenerateDto);
}
