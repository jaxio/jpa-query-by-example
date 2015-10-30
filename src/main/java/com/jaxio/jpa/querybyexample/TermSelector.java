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

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class TermSelector implements Serializable {
    private static final long serialVersionUID = 1L;
    private final PathHolder pathHolder;
    private List<String> selected = newArrayList();
    private boolean orMode = true;

    public TermSelector() {
        this.pathHolder = null;
    }

    public TermSelector(SingularAttribute<?, ?> attribute) {
        this.pathHolder = new PathHolder(attribute);
    }

    public SingularAttribute<?, ?> getAttribute() {
        return pathHolder != null ? (SingularAttribute<?, ?>) pathHolder.getAttributes().get(0) : null;
    }

    public boolean isOrMode() {
        return orMode;
    }

    public void setOrMode(boolean orMode) {
        this.orMode = orMode;
    }

    public TermSelector or() {
        setOrMode(true);
        return this;
    }

    public TermSelector and() {
        setOrMode(false);
        return this;
    }

    /*
     * Get the possible candidates for property.
     */
    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = newArrayList(selected);
    }

    /*
     * Set the possible candidates for property.
     */
    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public TermSelector selected(String... selected) {
        setSelected(newArrayList(selected));
        return this;
    }

    public boolean isNotEmpty() {
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        for (String word : selected) {
            if (isNotBlank(word)) {
                return true;
            }
        }
        return false;
    }

    public void clearSelected() {
        if (selected != null) {
            selected.clear();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (selected != null) {
            s.append("term");
            if (selected.size() > 1) {
                s.append('s');
            }
            s.append(": ");
            s.append(Arrays.toString(selected.toArray()));
        }
        if (pathHolder != null) {
            if (s.length() > 0) {
                s.append(' ');
            }
            s.append("on ");
            s.append(pathHolder.getPath());
        }
        return s.toString();
    }
}