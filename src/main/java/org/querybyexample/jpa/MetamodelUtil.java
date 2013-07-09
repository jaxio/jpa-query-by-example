package org.querybyexample.jpa;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Attribute;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class MetamodelUtil {

    private static Map<Class<?>, Class<?>> metamodelCache = Maps.newHashMap();

    public static List<Attribute<?, ?>> toMetamodelListAttributes(String path, Class<?> from) {
        try {
            List<Attribute<?, ?>> attributes = newArrayList();
            Class<?> current = from;
            for (String pathItem : Splitter.on(".").split(path)) {
                System.out.println(pathItem);
                Class<?> metamodelClass = getClazz(current);
                Attribute<?, ?> attribute = (Attribute<?, ?>) metamodelClass.getField(pathItem).get(null);
                attributes.add(attribute);
                current = attribute.getJavaType();
            }
            return attributes;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Class<?> getClazz(Class<?> current) throws ClassNotFoundException {
        if (metamodelCache.containsKey(current)) {
            return metamodelCache.get(current);
        }
        Class<?> metamodelClass = Class.forName(current.getName() + "_");
        metamodelCache.put(current, metamodelClass);
        return metamodelClass;
    }
}