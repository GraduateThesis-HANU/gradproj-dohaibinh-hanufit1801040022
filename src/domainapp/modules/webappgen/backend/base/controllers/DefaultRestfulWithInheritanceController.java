package domainapp.modules.webappgen.backend.base.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;
import domainapp.modules.webappgen.backend.base.services.InheritedCrudService;
import domainapp.modules.webappgen.backend.base.websockets.WebSocketHandler;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CompulsoryModule;
import examples.domainapp.modules.webappgen.backend.services.coursemodule.model.CourseModule;

import java.util.Collection;
import java.util.Objects;

/**
 * Default implementation of {@link #RestfulWithInheritanceController}
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class DefaultRestfulWithInheritanceController<T>
        extends DefaultRestfulController<T>
        implements RestfulWithInheritanceController<T> {

    public DefaultRestfulWithInheritanceController(WebSocketHandler webSocketHandler) {
        super(webSocketHandler);
    }

    @Override
    public Page<T> getEntityListByTypeAndPage(String type, PagingModel pagingModel) {
        return ((InheritedCrudService) getServiceOfGenericType(getGenericType()))
                .getEntityListByTypeAndPage(type, pagingModel);
    }

}
