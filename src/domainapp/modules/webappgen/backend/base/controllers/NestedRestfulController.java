package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.annotations.*;
import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;

import java.util.Collection;
import java.util.Map;

/**
 * Represent a nested (level-1) resource endpoint.
 * @param <TOuter> the outer type
 * @param <TInner> the inner (nested) type
 */
public interface NestedRestfulController<TOuter, TInner> {

    /**
     * Create an object instance of the inner type as owned by the outer instance.
     * @param outerId
     */
    @Create
    TInner createInner(@ID Identifier<?> outerId,
                       @Modifying TInner requestBody);

    /**
     * Retrieve a list of inner object instances owned by the outer.
     * @param outerId
     * @return
     */
    @Retrieve
    Page<TInner> getInnerListByOuterId(
            @ID Identifier<?> outerId,
            @PagingCondition PagingModel pagingModel);
}
