package com.sap.iot.kafka2hana.repository;

import com.sap.iot.kafka2hana.model.Measures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasuresRepository extends JpaRepository<Measures, Long> {
}
