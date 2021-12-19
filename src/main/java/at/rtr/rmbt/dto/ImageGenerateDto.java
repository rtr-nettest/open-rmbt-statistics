package at.rtr.rmbt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ImageGenerateDto {

    private final double download;
    private final double upload;
    private final double ping;
    private final String isp;
    private final String typ;
    private final String signal;
    private final String os;
    private final String size;
    private final String lang;
}
