package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.modules.webappgen.backend.base.websockets.WebSocketHandler;
import domainapp.modules.webappgen.backend.utils.ClassAssocUtils;
import domainapp.modules.webappgen.backend.utils.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link #RestfulController}
 */
@SuppressWarnings("unchecked")
public abstract class DefaultRestfulController<T> implements RestfulController<T> {

    private final Class<T> genericType =
        (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];

    private final WebSocketHandler webSocketHandler;

    public DefaultRestfulController(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    protected Class<T> getGenericType() {
        return genericType;
    }

    protected <U> CrudService<U> getServiceOfGenericType(Class<U> cls) {
        return ServiceRegistry.getInstance().get(cls.getSimpleName());
    }

    @Override
    public T createEntity(T inputEntity) {
        T createdEntity = getServiceOfGenericType(genericType)
            .createEntity(inputEntity);
        // server-push notification
        performServerPush();

        return createdEntity;
    }

    private void performServerPush() {
        List<Class<?>> associatedClasses = ClassAssocUtils.getAssociated(genericType);
        associatedClasses.add(genericType);
        webSocketHandler.handleServerPush(
                associatedClasses
                    .stream()
                    .map(Class::getSimpleName)
                    .map(StringUtils::toUrlEntityString)
                    .collect(Collectors.toList()));
    }

    @Override
    public Page<T> getEntityListByPage(PagingModel pagingModel) {
        return getServiceOfGenericType(genericType)
            .getEntityListByPage(pagingModel);
    }

    @Override
    public T getEntityById(Identifier<?> id) {
        return getServiceOfGenericType(genericType)
            .getEntityById(id);
    }

    @Override
    public T updateEntity(Identifier<?> id, T updatedInstance) {
        T updatedEntity = getServiceOfGenericType(genericType)
            .updateEntity(id, updatedInstance);
        // server-push notification
        performServerPush();
        return updatedEntity;
    }

    @Override
    public void deleteEntityById(Identifier<?> id) {
        getServiceOfGenericType(genericType).deleteEntityById(id);
        // server-push notification
        performServerPush();
    }

}
