package com.challengeteam.shop.mapper;

import java.util.List;

/**
 * Base mapper interface for converting between entity and DTO objects.
 *
 * @param <E> the entity type
 * @param <D> the DTO (Data Transfer Object) type
 */
public interface BaseMapper<E, D> {
    /**
     * Converts an entity to a DTO.
     *
     * @param entity the entity to convert
     * @return the corresponding DTO, or null if the entity is null
     */
    D toDto(E entity);

    /**
     * Converts a DTO to an entity.
     *
     * @param dto the DTO to convert
     * @return the corresponding entity, or null if the DTO is null
     */
    E toEntity(D dto);

    /**
     * Converts a list of DTOs to a list of entities.
     *
     * @param dtoList the list of DTOs to convert
     * @return the corresponding list of entities
     */
    List<E> toEntityList(List<D> dtoList);

    /**
     * Converts a list of entities to a list of DTOs.
     *
     * @param entityList the list of entities to convert
     * @return the corresponding list of DTOs
     */
    List<D> toDtoList(List<E> entityList);
}