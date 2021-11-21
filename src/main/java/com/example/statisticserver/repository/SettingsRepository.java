package com.example.statisticserver.repository;


import com.example.statisticserver.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findFirstByKeyAndLangIsNullOrKeyAndLangOrderByLang(String key, String key2, String lang);
}
