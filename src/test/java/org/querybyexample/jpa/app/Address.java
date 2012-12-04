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
import javax.persistence.Version;
import org.querybyexample.jpa.Identifiable;

@Entity
@Table(name = "ADDRESS")
public class Address implements Identifiable<Integer>, Serializable {
	private static final long serialVersionUID = 1L;

	// Raw attributes
	private Integer id; // pk
	private String streetName;
	private String city; // not null
	private Integer version;

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

	// -- [streetName] ------------------------

	@Column(name = "STREET_NAME", length = 100)
	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	// -- [city] ------------------------

	@Column(name = "CITY", nullable = false, length = 100)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	// -- [version] ------------------------

	@Column(name = "VERSION", precision = 10)
	@Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}