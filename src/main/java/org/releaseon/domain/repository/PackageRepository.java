package org.releaseon.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.releaseon.domain.entity.Package;

public interface PackageRepository extends CrudRepository <Package, String > {

}
