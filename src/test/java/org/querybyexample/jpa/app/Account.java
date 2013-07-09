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

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.querybyexample.jpa.Identifiable;

@Entity
@Table(name = "ACCOUNT")
public class Account implements Identifiable<String>, Serializable {
    private static final long serialVersionUID = 1L;

    // Raw attributes
    private String id; // pk
    private String username; // unique (not null)
    private String password; // not null
    private String email; // unique (not null)
    private Integer favoriteNumber;
    private Boolean isEnabled;
    private Date birthDate;

    // Technical attributes for query by example
    private Integer addressId;

    // Many to one
    private Address homeAddress; // (addressId)

    // Many to many
    private List<Role> roles = new ArrayList<Role>();

    public Account() {
    }

    public Account(Role... roles) {
        for (Role role : roles) {
            addRole(role);
        }
    }

    public Account(Address homeAddress) {
        setHomeAddress(homeAddress);
    }

    // -------------------------------
    // Getter & Setter
    // -------------------------------

    // -- [id] ------------------------

    @Column(name = "ID", length = 32)
    @GeneratedValue(generator = "strategy-uuid")
    @GenericGenerator(name = "strategy-uuid", strategy = "uuid")
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Transient
    public boolean isIdSet() {
        return id != null && !id.isEmpty();
    }

    // -- [username] ------------------------

    @Column(name = "LOGIN", nullable = false, unique = true, length = 100)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // -- [password] ------------------------

    @Column(name = "`PASSWORD`", nullable = false, length = 100)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // -- [email] ------------------------

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // -- [favoriteNumber] ------------------------

    @Column(name = "FAVORITE_NUMBER")
    public Integer getFavoriteNumber() {
        return favoriteNumber;
    }

    public void setFavoriteNumber(Integer favoriteNumber) {
        this.favoriteNumber = favoriteNumber;
    }

    // -- [isEnabled] ------------------------

    @Column(name = "IS_ENABLED", length = 1)
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    // -- [birthDate] ------------------------

    @Column(name = "BIRTH_DATE", length = 23)
    @Temporal(TIMESTAMP)
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    // -- [addressId] ------------------------

    @Column(name = "ADDRESS_ID", precision = 10, insertable = false, updatable = false)
    public Integer getAddressId() {
        return addressId;
    }

    private void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    // --------------------------------------------------------------------
    // Many to One support
    // --------------------------------------------------------------------

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // many-to-one: Account.addressId ==> Address.id
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @JoinColumn(name = "ADDRESS_ID")
    @ManyToOne(cascade = PERSIST, fetch = LAZY)
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * Set the homeAddress without adding this Account instance on the passed homeAddress If you want to preserve referential integrity we recommend to use
     * instead the corresponding adder method provided by {@link Address}
     */
    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;

        // We set the foreign key property so it can be used by our JPA search by Example facility.
        if (homeAddress != null) {
            setAddressId(homeAddress.getId());
        } else {
            setAddressId(null);
        }
    }

    // --------------------------------------------------------------------
    // Many to Many
    // --------------------------------------------------------------------

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // many-to-many: account ==> roles
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Returns the roles list.
     */
    @JoinTable(name = "ACCOUNT_ROLE", joinColumns = @JoinColumn(name = "ACCOUNT_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    @ManyToMany(cascade = PERSIST)
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Set the roles list.
     * <p>
     * It is recommended to use the helper method {@link #addRole(Role)} / {@link #removeRole(Role)} if you want to preserve referential integrity at the object
     * level.
     * 
     * @param roles the list of Role
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Helper method to add the passed {@link Role} to the roles list.
     */
    public boolean addRole(Role role) {
        return getRoles().add(role);
    }

    /**
     * Helper method to remove the passed {@link Role} from the roles list.
     */
    public boolean removeRole(Role role) {
        return getRoles().remove(role);
    }

    /**
     * Helper method to determine if the passed {@link Role} is present in the roles list.
     */
    public boolean containsRole(Role role) {
        return getRoles() != null && getRoles().contains(role);
    }
}