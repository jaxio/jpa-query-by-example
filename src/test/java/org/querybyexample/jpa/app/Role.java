/*
 *  Copyright 2012 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.querybyexample.jpa.app;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.querybyexample.jpa.Identifiable;

@Entity
@Table(name = "`ROLE`")
public class Role implements Identifiable<Integer>, Serializable {
	private static final long serialVersionUID = 1L;

	// Raw attributes
	private Integer id; // pk
	private String roleName; // unique (not null)

	// -------------------------------
	// Getter & Setter
	// -------------------------------

	// -- [id] ------------------------

	@Column(name = "ID", precision = 10)
	@GeneratedValue
	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Transient
	public boolean isIdSet() {
		return id != null;
	}

	// -- [roleName] ------------------------

	@Column(name = "ROLE_NAME", nullable = false, unique = true, length = 100)
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}