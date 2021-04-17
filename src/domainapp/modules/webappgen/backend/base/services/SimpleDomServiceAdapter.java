package domainapp.modules.webappgen.backend.base.services;

import domainapp.basics.controller.ControllerTk;
import domainapp.basics.controller.helper.DataValidator;
import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.modules.webappgen.backend.utils.IdentifierUtils;
import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class SimpleDomServiceAdapter<T> implements CrudService<T> {
    protected final SoftwareImpl sw;
    protected Class<T> type;
    protected BiConsumer<Identifier, Object> onCascadeUpdate;

    // autowired constructor
    protected SimpleDomServiceAdapter(SoftwareImpl sw) {
        this(null, sw);
    }

    public SimpleDomServiceAdapter(Class<T> type, SoftwareImpl sw) {
        this.type = type;
        this.sw = sw;
    }

    public Class<T> getType() {
        return type;
    }

    protected void setType(Class<T> type) {
        this.type = type;
    }

    protected static <T> void validateObject(T input, SoftwareImpl sw)
            throws ConstraintViolationException {
        Class<T> cls = (Class) input.getClass();
        DataValidator<T> validator =
                ControllerTk.getDomainSpecificDataValidator(sw.getDODM(), cls);
        List<Field> attrFields = Stream.of(cls.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(DAttr.class))
                .collect(Collectors.toList());
        for (Field field : attrFields) {
            DAttr attr = field.getAnnotation(DAttr.class);
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(input);
                validator.validateDomainValue(attr, fieldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public T createEntity(T entity) {
        try {
            validateObject(entity, sw);
            sw.addObject((Class<T>) entity.getClass(), entity);

            // cascade update
            Class entityClass = entity.getClass();
            String[] fieldNames = getFieldNames(entityClass, type);
            Object[] values = getFieldValues(entityClass, type, fieldNames, entity);
            performCascadeUpdate(values);

            return entity;
        } catch (DataSourceException | ConstraintViolationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getEntityById(Identifier<?> id) {
        try {
            T retrieved = sw.retrieveObjectById(type, id.getId());
            try {
                sw.loadAssociatedObjects(retrieved);
            } catch (NullPointerException ex) { }
            return retrieved;
        } catch (NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<T> getEntityListByPage(PagingModel pagingModel) {
        Collection<T> entities = this.getAllEntities();
        return paginate(entities, pagingModel);
    }

    protected Page<T> paginate(Collection<T> entities, PagingModel pagingModel) {
        final int pageNumber = pagingModel.getPage();
        final int itemPerPage = pagingModel.getCount();

        if (entities == null || entities.isEmpty()) {
            return Page.empty();
        }
        final int size = entities.size();
        final int skip = (pageNumber - 1) * itemPerPage;
        if (skip > size) {
            throw new NoSuchElementException("Not found: Page #" + pageNumber);
        }
        final int pageCount = size / itemPerPage + size % itemPerPage > 0 ? 1 : 0;
        final Collection<T> pageContent = entities.stream().skip(skip).limit(itemPerPage).collect(Collectors.toList());
        return new Page<>(pageNumber, pageCount, pageContent);
    }

    @Override
    public Collection<T> getAllEntities() {
        try {
            Collection<T> entities = sw.retrieveObjects(type, "id", Op.GT, "0");
            if (entities == null) entities = new ArrayList<>();
            for (T entity : entities) {
                try {
                    sw.loadAssociatedObjects(entity);
                } catch (NullPointerException ex) { }
            }
            return entities;
        } catch (NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getFieldNames(Class cls, Class... superClasses) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(List.of(cls.getDeclaredFields()));
        fields.addAll(List.of(superClasses[0].getDeclaredFields()));

        List<String> fieldNames = fields.stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers())
                    && Modifier.isPrivate(field.getModifiers()))
                .filter(field -> field.isAnnotationPresent(DAttr.class))
                .map(field -> field.getAnnotation(DAttr.class))
                .filter(attr -> !attr.id() && !attr.auto() && !attr.virtual())
                .map(DAttr::name)
                .collect(Collectors.toList());
        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    private static <T> Object[] getFieldValues(Class<T> cls, Class<T> superClass, String[] fieldNames, T o) {

        return List.of(fieldNames)
                .stream()
                .map(name -> {
                    try {
                        Field field = cls.getDeclaredField(name);
                        field.setAccessible(true);
                        return field.get(o);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
//                        e.printStackTrace();
                        try {
                            Field field = superClass.getDeclaredField(name);
                            field.setAccessible(true);
                            return field.get(o);
                        } catch (NoSuchFieldException | IllegalAccessException ex) {
                            return null;
                        }
                    }
                })
                .toArray();
    }

    @Override
    public T updateEntity(Identifier<?> id, T entity) {
        try {
            validateObject(entity, sw);
            if (!id.getId().toString().equals(
                IdentifierUtils.getIdField(getType()).get(entity).toString())) return null;
            Class<T> entityClass = (Class)entity.getClass();
            T oldEntity = sw.retrieveObjectById(entityClass, id.getId());

            if (entity.equals(oldEntity)) return entity;
            String[] fieldNames = getFieldNames(entityClass, type);
            Object[] values = getFieldValues(entityClass, type, fieldNames, entity);
            sw.updateObject(entityClass, oldEntity, fieldNames, values);

            performCascadeUpdate(values);

            return sw.retrieveObjectById(entityClass, id.getId());
        } catch (NotPossibleException | NotFoundException
                | DataSourceException | IllegalArgumentException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void performCascadeUpdate(Object[] values) throws IllegalAccessException {
        // cascade update
        for (Object obj : values) {
            if (obj == null) continue;
            Class<?> cls = obj.getClass();
            if (!cls.isAnnotationPresent(DClass.class)) continue;
            Identifier identifier = null;
            for (Field field : cls.getDeclaredFields()) {
                if (!field.isAnnotationPresent(DAttr.class)) continue;
                DAttr dAttr = field.getAnnotation(DAttr.class);
                if (!dAttr.id()) continue;
                field.setAccessible(true);
                identifier = Identifier.fromString(field.get(obj).toString());
            }
            if (identifier == null) continue;
            if (this.onCascadeUpdate != null) {
                this.onCascadeUpdate.accept(identifier, obj);
            }
        }
    }

    @Override
    public void deleteEntityById(Identifier<?> id) {
        try {
            T toDelete = sw.retrieveObjectById(type, id.getId());
            Class entityClass = toDelete.getClass();
            String[] fieldNames = getFieldNames(entityClass, type);
            Object[] values = getFieldValues(entityClass, type, fieldNames, toDelete);

            sw.deleteObject(toDelete, type);
            sw.getDom().getAssociates(toDelete, toDelete.getClass())
                    .forEach(associate -> {
                        System.out.println(associate.getAssociateObj());
                        if (associate.getAssociationType() == DAssoc.AssocType.One2One) {
                            try {
                                sw.deleteObject(associate.getAssociateObj(), associate.getAssociateClass());
                            } catch (DataSourceException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            sw.deleteObject(toDelete, type);

            // cascade update
//            performCascadeUpdate(values);
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setOnCascadeUpdate(BiConsumer<Identifier, Object> handler) {
        this.onCascadeUpdate = handler;
    }
}
