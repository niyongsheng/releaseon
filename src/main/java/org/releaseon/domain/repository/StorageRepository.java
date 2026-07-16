package org.releaseon.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.releaseon.domain.entity.Storage;

public interface StorageRepository extends CrudRepository<Storage, String> {

    @Query("select s from Storage s where s.key=:key")
    public Storage findByKey(@Param("key") String key);
}
