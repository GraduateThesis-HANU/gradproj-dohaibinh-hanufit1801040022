package domainapp.modules.webappgen.backend.base.services;

import domainapp.basics.controller.ControllerTk;
import domainapp.basics.controller.helper.DataValidator;
import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.def.Associate;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.modules.domevents.CMEventType;
import domainapp.modules.domevents.Publisher;
import domainapp.modules.webappgen.backend.utils.IdentifierUtils;
import domainapp.modules.webappgen.backend.base.models.Identifier;
import domainapp.modules.webappgen.backend.base.models.Page;
import domainapp.modules.webappgen.backend.base.models.PagingModel;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.query.Expression.Op;
import domainapp.softwareimpl.SoftwareImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
            Object[] values = getFieldValues(entityClass, type, fieldNames, entity)
                    .values().toArray();
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

    private static boolean isOneOneOrManyOneAssocField(Field field) {
        if (!field.isAnnotationPresent(DAssoc.class)) return false;
        DAssoc dAssoc = field.getAnnotation(DAssoc.class);
        return dAssoc.ascType().equals(DAssoc.AssocType.One2One)
                || (dAssoc.ascType().equals(DAssoc.AssocType.One2Many)
                    && dAssoc.endType().equals(DAssoc.AssocEndType.Many));
    }

    private static Field getFieldByName(String name, Class cls, Class superClass) {
        try {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
//                        e.printStackTrace();
            try {
                Field field = superClass.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
    }

    //  if assoc 1-1 or M-1
    //      allow null
    //  else
    //      reject new value
    // SIDE EFFECT: Null assoc values are mapped instantly to REMOVE_LINK
    private static <T> Map<String, Object> getFieldValues(
            Class<T> cls, Class<T> superClass, String[] fieldNames, T o) {
        final Map<String, Object> toBeUpdated = new HashMap<>();

        List<String> fieldNameList = List.of(fieldNames);
        List<String> nullableFieldNames = fieldNameList.stream()
                .map(name -> getFieldByName(name, cls, superClass))
                .filter(Objects::nonNull)
                .filter(SimpleDomServiceAdapter::isOneOneOrManyOneAssocField)
                .map(Field::getName)
                .collect(Collectors.toList());
        List<String> nonNullableFieldNames = fieldNameList.stream()
                .filter(name -> !nullableFieldNames.contains(name))
                .collect(Collectors.toList());

        nullableFieldNames
                .stream()
                .map(name -> getFieldByName(name, cls, superClass))
                .forEach(field -> {
                    try {
                        toBeUpdated.put(field.getName(), field.get(o));
                    } catch (IllegalAccessException | NullPointerException e) {
                        toBeUpdated.put(field.getName(), null);
                    }
                });

        nonNullableFieldNames
                .stream()
                .map(name -> getFieldByName(name, cls, superClass))
                .filter(Objects::nonNull)
                .forEach(field -> {
                    try {
                        toBeUpdated.put(field.getName(), field.get(o));
                    } catch (IllegalAccessException | NullPointerException e) {
                        toBeUpdated.put(field.getName(), null);
                    }
                });

        return toBeUpdated;
    }

    @Override
    public T updateEntity(Identifier<?> id, T entity) {
        try {
            validateObject(entity, sw);
            if (!id.getId().toString().equals(
                IdentifierUtils.getIdField(getType()).get(entity).toString())) return null;
            Class<T> entityClass = (Class)entity.getClass();
            T oldEntity = sw.retrieveObjectById(entityClass, id.getId());

            if (entity == oldEntity) return entity;
            String[] fieldNames = getFieldNames(entityClass, type);
            Map<String, Object> updateValues = getFieldValues(entityClass, type, fieldNames, entity);
            final int numOfUpdateValues = updateValues.size();
            String[] updateFieldNames = updateValues.keySet().toArray(new String[numOfUpdateValues]);
            Object[] updateFieldValues = updateValues.values().toArray(new Object[numOfUpdateValues]);
            sw.updateObject(entityClass, oldEntity, updateFieldNames, updateFieldValues);

            performCascadeUpdate(updateFieldValues);

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
            if (toDelete == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
            }
            Class entityClass = toDelete.getClass();

            sw.deleteObject(toDelete, type);
            Collection<Associate> associates = sw.getDom().getAssociates(toDelete, toDelete.getClass());
            if (associates != null) {
                associates.forEach(associate -> {
                    System.out.println(associate.getAssociateObj());
                    if (associate.isAssociationType(DAssoc.AssocType.One2One)) {
                        try {
                            sw.deleteObject(associate.getAssociateObj(), associate.getAssociateClass());
                        } catch (DataSourceException e) {
                            e.printStackTrace();
                        }
                    } if (toDelete instanceof Publisher) {
                        // remove link
                        Publisher eventSourceObj = (Publisher) toDelete;
                        eventSourceObj.notify(CMEventType.OnRemoved, eventSourceObj.getEventSource());
                        eventSourceObj.removeAllSubscribers();
                    }
                });
            }
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
