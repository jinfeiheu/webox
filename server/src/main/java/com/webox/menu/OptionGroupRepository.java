package com.webox.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {

    /** Ids of dishes (within the given set) that have at least one required option group. */
    @Query("SELECT DISTINCT g.dish.id FROM OptionGroup g WHERE g.required = true AND g.dish.id IN :dishIds")
    List<Long> findDishIdsWithRequiredGroups(@Param("dishIds") Collection<Long> dishIds);
}
