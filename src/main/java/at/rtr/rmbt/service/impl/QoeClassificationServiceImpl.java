package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.dto.QoeClassificationThresholds;
import at.rtr.rmbt.mapper.QoeClassificationMapper;
import at.rtr.rmbt.repository.QoeClassificationRepository;
import at.rtr.rmbt.service.QoeClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QoeClassificationServiceImpl implements QoeClassificationService {

    private final QoeClassificationRepository qoeClassificationRepository;
    private final QoeClassificationMapper qoeClassificationMapper;

    private final List<QoeClassificationThresholds> cachedThresholds = new ArrayList<>();
    private Instant lastCacheLoadTime;

    @PostConstruct
    public void initCache() {
        reloadCache();
    }

    @Override
    public List<QoeClassificationThresholds> getQoeClassificationThreshold() {
        // If more than 30 minutes have passed since last load, refresh
        if (Instant.now().minus(30, ChronoUnit.MINUTES).isAfter(lastCacheLoadTime)) {
            reloadCache();
        }
        return cachedThresholds;
    }

    private void reloadCache() {
        cachedThresholds.clear();
        cachedThresholds.addAll(
                qoeClassificationRepository.findAll().stream()
                        .map(qoeClassificationMapper::qoeClassificationToQoeClassificationThresholds)
                        .toList()
        );
        lastCacheLoadTime = Instant.now();
    }
}