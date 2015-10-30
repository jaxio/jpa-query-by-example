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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.metamodel.Attribute;
import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jaxio.jpa.querybyexample.OrderByDirection.ASC;
import static com.jaxio.jpa.querybyexample.OrderByDirection.DESC;

/**
 * Holder class for search ordering used by the {@link SearchParameters}.
 */
public class OrderBy implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PathHolder pathHolder;
    private OrderByDirection direction = ASC;

    public OrderBy(OrderByDirection direction, Attribute<?, ?>... attributes) {
        this.direction = checkNotNull(direction);
        this.pathHolder = new PathHolder(checkNotNull(attributes));
    }

    public OrderBy(OrderByDirection direction, String path, Class<?> from) {
        this.direction = checkNotNull(direction);
        this.pathHolder = new PathHolder(checkNotNull(path), checkNotNull(from));
    }

    public List<Attribute<?, ?>> getAttributes() {
        return pathHolder.getAttributes();
    }

    public String getPath() {
        return pathHolder.getPath();
    }

    public OrderByDirection getDirection() {
        return direction;
    }

    public boolean isOrderDesc() {
        return DESC == direction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pathHolder == null) ? 0 : pathHolder.hashCode());
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
        OrderBy other = (OrderBy) obj;
        if (pathHolder == null) {
            if (other.pathHolder != null) {
                return false;
            }
        } else if (!pathHolder.equals(other.pathHolder)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}