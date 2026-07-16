package org.releaseon.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.releaseon.domain.entity.WebHook;

public interface WebHookRepository extends CrudRepository <WebHook, String > {

}
