/*
 * Copyright 2015 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaxio.jpa.querybyexample;

import javax.persistence.metamodel.Attribute;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Holder class for path used by the {@link OrderBy}, {@link PropertySelector}, {@link TermSelector} and {@link SearchParameters}.
 */
public class PathHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String path;
    private final Class<?> from;
    private transient List<Attribute<?, ?>> attributes;

    public PathHolder(Attribute<?, ?>... attributes) {
        this(newArrayList(attributes));
    }

    public PathHolder(List<Attribute<?, ?>> attributes) {
        JpaUtil.getInstance().verifyPath(checkNotNull(attributes));
        this.attributes = newArrayList(attributes);
        this.path = MetamodelUtil.getInstance().toPath(attributes);
        this.from = attributes.get(0).getDeclaringType().getJavaType();
    }

    public PathHolder(String path, Class<?> from) {
        this.path = path;
        this.from = from;
        // to verify path
        getAttributes();
    }

    public List<Attribute<?, ?>> getAttributes() {
        if (attributes == null) {
            attributes = MetamodelUtil.getInstance().toAttributes(path, from);
        }
        return attributes;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PathHolder other = (PathHolder) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

}