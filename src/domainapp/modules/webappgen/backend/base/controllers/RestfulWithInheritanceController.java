package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.annotations.PagingCondition;
import domainapp.modules.webappgen.backend.annotations.ResourceController;
import domainapp.modules.webappgen.backend.annotations.Retrieve;
import domainapp.modules.webappgen.backend.annotations.Subtype;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;

@ResourceController
public interface RestfulWithInheritanceController<T>
        extends RestfulController<T> {
    /**
     * Retrieve a paginated list of entities of type T by one of its subtype
     * (if specified).
     * @param page
     * @param count
     */
    @Retrieve
    Page<T> getEntityListByTypeAndPage(
        @Subtype String type, @PagingCondition PagingModel pagingModel);

    /**
     * Retrieve a paginated list of entities of type T.
     * @param pageNumber
     * @param count
     */
    @Retrieve(ignored = true)
    @Override
    Page<T> getEntityListByPage(@PagingCondition PagingModel pagingModel);
}
