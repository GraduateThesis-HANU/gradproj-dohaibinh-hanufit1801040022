package domainapp.modules.webappgen.backend.base.services;

import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;

import java.util.Collection;

public interface InheritedCrudService<T>
        extends CrudService<T> {
    Collection<T> getEntityListByType(String type);
    Page<T> getEntityListByTypeAndPage(String type, PagingModel pagingModel);
}
