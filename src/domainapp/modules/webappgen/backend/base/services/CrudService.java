package domainapp.modules.webappgen.backend.base.services;

import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface CrudService<T> {
    T createEntity(T entity);
    T getEntityById(Identifier<?> id);
    Page<T> getEntityListByPage(PagingModel pagingModel);
    Collection<T> getAllEntities();
    T updateEntity(Identifier<?> id, T entity);
    void deleteEntityById(Identifier<?> id);

    void setOnCascadeUpdate(BiConsumer<Identifier, Object> handler);
}
