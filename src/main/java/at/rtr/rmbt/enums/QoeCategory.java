package at.rtr.rmbt.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public enum QoeCategory {
    STREAMING_AUDIO_STREAMING("streaming_audio_streaming"),
    VIDEO_SD("video_sd"),
    VIDEO_HD("video_hd"),
    VIDEO_UHD("video_uhd"),
    GAMING("gaming"),
    GAMING_CLOUD("gaming_cloud"),
    GAMING_STREAMING("gaming_streaming"),
    GAMING_DOWNLOAD("gaming_download"),
    VOIP("voip"),
    VIDEO_TELEPHONY("video_telephony"),
    VIDEO_CONFERENCING("video_conferencing"),
    MESSAGING("messaging"),
    WEB("web"),
    CLOUD("cloud");
    @Enumerated(EnumType.STRING)
    private final String value;

    @JsonCreator
    public static QoeCategory forValue(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        for (QoeCategory category : QoeCategory.values()) {
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown QoeCategory: " + value);
    }
}
