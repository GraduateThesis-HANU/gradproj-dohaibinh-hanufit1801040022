package domainapp.modules.webappgen.backend.base.controllers;

import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.services.CrudService;
import domainapp.basics.model.meta.DOpt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class DefaultNestedRestfulController<T1, T2>
        implements NestedRestfulController<T1, T2> {

    protected Class<T1> outerType = (Class) ((ParameterizedType) getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0];
    protected Class<T2> innerType = (Class) ((ParameterizedType) getClass()
        .getGenericSuperclass()).getActualTypeArguments()[1];

    protected <X> CrudService<X> getServiceOfGenericType(String clsName) {
        return ServiceRegistry.getInstance().get(clsName);
    }

    @Override
    public T2 createInner(Identifier<?> outerId, T2 requestBody) {
        // TODO: FIX THIS!

        try {
            CrudService<T2> svc = getServiceOfGenericType(innerType.getCanonicalName());
            T1 outer = (T1) getServiceOfGenericType(outerType.getCanonicalName()).getEntityById(outerId);
            T2 created = svc.createEntity(requestBody);
            getLinkAdder(outerType, innerType).invoke(outer, created);
            return created;
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Method getMethodByName(Class<?> cls, String name, Class<?>... parameters) {
        try {
            return cls.getMethod(name, parameters);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Method getLinkAdder(Class<?> cls, Class<?> toAdd) {
        for (Method m : cls.getMethods()) {
            DOpt dopt = m.getAnnotation(DOpt.class);
            if (dopt != null && dopt.type() == DOpt.Type.LinkAdderNew
                    && m.getParameters()[0].getType() == toAdd) {
                return m;
            }
        }
        return null;
    }

    private static Constructor<?> getRequiredConstructor(Class<?> cls) {
        for (Constructor<?> c : cls.getConstructors()) {
            boolean isReqConstructor = false;
            DOpt[] dopts = c.getAnnotationsByType(DOpt.class);
            for (DOpt d : dopts) {
                if (d.type().equals(DOpt.Type.RequiredConstructor)) {
                    isReqConstructor = true;
                    break;
                }
            }
            if (isReqConstructor) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Collection<T2> getInnerListByOuterId(Identifier<?> outerId) {
        // reflection-based solution -- needs HEAVY optimization
        CrudService<T1> svc = getServiceOfGenericType(outerType.getCanonicalName());
        T1 outerById = svc.getEntityById(outerId);
        String getInnerMethodName = "get" + innerType.getSimpleName() + "s";
        Method getInnersFromOuter = getMethodByName(outerType, getInnerMethodName);
        try {
            return (Collection<T2>) getInnersFromOuter.invoke(outerById);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
