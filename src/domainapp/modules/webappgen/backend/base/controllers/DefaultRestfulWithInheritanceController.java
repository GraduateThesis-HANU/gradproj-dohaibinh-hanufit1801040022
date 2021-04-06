package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;
import domainapp.modules.webappgen.backend.base.services.InheritedCrudService;

/**
 * Default implementation of {@link #RestfulWithInheritanceController}
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DefaultRestfulWithInheritanceController<T> extends DefaultRestfulController<T>
        implements RestfulWithInheritanceController<T> {

    @Override
    public Page<T> getEntityListByTypeAndPage(String type, PagingModel pagingModel) {
        return ((InheritedCrudService) getServiceOfGenericType(getGenericType()))
                .getEntityListByTypeAndPage(type, pagingModel);
    }

}
