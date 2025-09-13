package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.dto.QoeClassificationThresholds;
import at.rtr.rmbt.mapper.QoeClassificationMapper;
import at.rtr.rmbt.repository.QoeClassificationRepository;
import at.rtr.rmbt.service.QoeClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QoeClassificationServiceImpl implements QoeClassificationService {

    private final QoeClassificationRepository qoeClassificationRepository;
    private final QoeClassificationMapper qoeClassificationMapper;

    // Holds the cached results
    private final List<QoeClassificationThresholds> cachedThresholds = new ArrayList<>();

    @PostConstruct
    public void initCache() {
        // Load the table data once at startup
        cachedThresholds.addAll(
                qoeClassificationRepository.findAll().stream()
                        .map(qoeClassificationMapper::qoeClassificationToQoeClassificationThresholds)
                        .toList()
        );
    }

    @Override
    public List<QoeClassificationThresholds> getQoeClassificationThreshold() {
        // Return the cached list without querying the database again
        return cachedThresholds;
    }
}

