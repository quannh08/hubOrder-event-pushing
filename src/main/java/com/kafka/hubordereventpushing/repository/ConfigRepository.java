package com.kafka.hubordereventpushing.repository;

import com.kafka.hubordereventpushing.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config,Long> {
    Optional<Config> findByKey(String id);


}
