package domainapp.modules.webappgen.frontend.utils;

import domainapp.basics.model.meta.DAttr;

import java.lang.reflect.Field;

public final class IdentifierUtils {
    public static Field getIdField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            DAttr attrInfo = f.getAnnotation(DAttr.class);
            if (attrInfo == null) continue;
            if (attrInfo.id()) {
                f.setAccessible(true);
                return f; // because only 1 ID field
            }
        }
        return null; // no ID field
    }

    public static Object getIdOf(Object item) {
        if (item == null) return null;
        Class<?> cls = item.getClass();
        Field f = getIdField(cls);
        try {
            return f.get(item);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
