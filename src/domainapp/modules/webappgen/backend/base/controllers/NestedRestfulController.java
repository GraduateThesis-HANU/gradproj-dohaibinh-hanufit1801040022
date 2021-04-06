package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.annotations.Create;
import domainapp.modules.webappgen.backend.annotations.ID;
import domainapp.modules.webappgen.backend.annotations.Modifying;
import domainapp.modules.webappgen.backend.annotations.Retrieve;
import domainapp.modules.webappgen.backend.base.models.Identifier;

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
                       @Modifying Map<String, Object> requestBody);

    /**
     * Retrieve a list of inner object instances owned by the outer.
     * @param outerId
     */
    @Retrieve
    Collection<TInner> getInnerListByOuterId(@ID Identifier<?> outerId);
}
