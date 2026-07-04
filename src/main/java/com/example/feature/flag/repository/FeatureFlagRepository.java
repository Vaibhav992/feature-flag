package com.example.feature.flag.repository;

import com.example.feature.flag.entity.FeatureFlag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

	Optional<FeatureFlag> findByFeatureName(String featureName);
}
