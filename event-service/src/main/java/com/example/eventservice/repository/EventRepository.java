package com.example.eventservice.repository;

import com.example.eventservice.dto.request.EventFilter;
import com.example.eventservice.dto.request.EventParameter;
import com.example.eventservice.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.CriteriaDefinition;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final CassandraTemplate cassandraTemplate;

    public Optional<Event> loadEvent(String eventId, String deviceId) {
        var query = Query.query(
                Criteria.where("device_id").is(deviceId),
                Criteria.where("event_id").is(eventId)
        );

        return Optional.ofNullable(
                cassandraTemplate.selectOne(query, Event.class)
        );
    }

    public Slice<Event> loadEventsByFilters(EventParameter eventParameter) {
        var pageable = PageRequest.of(eventParameter.page(), eventParameter.pageSize());
        var criteriaDefinitions = getCriteriaDefinitionsByFilters(eventParameter);

        return cassandraTemplate.slice(
                Query.query(criteriaDefinitions).pageRequest(pageable).withAllowFiltering(),
                Event.class
        );
    }

    private List<CriteriaDefinition> getCriteriaDefinitionsByFilters(EventParameter eventParameter) {
        var criteriaDefinitions = new ArrayList<CriteriaDefinition>();

        var filter = eventParameter.eventFilter();
        getCriteriaDefinitionWhereIfFilterPresent(eventParameter.deviceId(), "device_id").ifPresent(criteriaDefinitions::add);
        getCriteriaDefinitionWhereIfFilterPresent(filter.type(), "type").ifPresent(criteriaDefinitions::add);

        var criteriaDefinitionTimestamp = getCriteriaDefinitionsTimestamp(filter);
        criteriaDefinitions.addAll(criteriaDefinitionTimestamp);

        return criteriaDefinitions;
    }

    private List<CriteriaDefinition> getCriteriaDefinitionsTimestamp(EventFilter filter) {
        Long fromTimestamp = filter.fromTimestamp();
        Long toTimestamp = filter.toTimestamp();

        String columnNameTimestamp = "timestamp";

        if (fromTimestamp != null && toTimestamp != null) {
            return List.of(
                    Criteria.where(columnNameTimestamp).gte(fromTimestamp),
                    Criteria.where(columnNameTimestamp).lte(toTimestamp)
            );
        } else if (fromTimestamp != null) {
            return List.of(
                    Criteria.where(columnNameTimestamp).gte(fromTimestamp)
            );
        } else if (toTimestamp != null) {
            return List.of(
                    Criteria.where(columnNameTimestamp).lte(toTimestamp)
            );
        } else {
            return List.of();
        }
    }

    private <T> Optional<CriteriaDefinition> getCriteriaDefinitionWhereIfFilterPresent(T filter, String fieldName) {
        if (filter != null) {
            return Optional.of(
                    Criteria.where(fieldName).is(filter)
            );

        } else {
            return Optional.empty();
        }
    }
}
