package at.rtr.rmbt.repository;

import at.rtr.rmbt.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read only through SettingsCache. */
public interface SettingsRepository extends JpaRepository<Settings, Long> {
}
