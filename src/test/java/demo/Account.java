/*
 * Source code generated by Celerio, a Jaxio product.
 * Documentation: http://www.jaxio.com/documentation/celerio/
 * Follow us on twitter: @jaxiosoft
 * Need commercial support ? Contact us: info@jaxio.com
 * Template pack-backend-jpa:src/main/java/domain/Entity.e.vm.java
 * Template is part of Open Source Project: https://github.com/jaxio/pack-backend-jpa
 */
package demo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.jaxio.jpa.querybyexample.Identifiable;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "ACCOUNT")
public class Account implements Identifiable<Integer>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(Account.class);

    // Raw attributes
    private Integer id;
    private String username;
    private String lastName;
    private Date birthDate;

    // Many to one
    private Address homeAddress;

    @Override
    @Column(name = "ID", precision = 10)
    @GeneratedValue
    @Id
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    @Transient
    public boolean isIdSet() {
        return id != null;
    }

    // -- [username] ------------------------

    @Column(name = "USERNAME", nullable = false, unique = true, length = 100)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Account username(String username) {
        setUsername(username);
        return this;
    }

    // -- [lastName] ------------------------

    @Column(name = "LAST_NAME", length = 255)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Account lastName(String lastName) {
        setLastName(lastName);
        return this;
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

    public Account birthDate(Date birthDate) {
        setBirthDate(birthDate);
        return this;
    }

    // -----------------------------------------------------------------
    // Many to One support
    // -----------------------------------------------------------------

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // many-to-one: Account.homeAddress ==> Address.id
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @JoinColumn(name = "ADDRESS_ID")
    @ManyToOne(cascade = { PERSIST, MERGE }, fetch = LAZY)
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * Set the {@link #homeAddress} without adding this Account instance on the passed {@link #homeAddress}
     */
    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public Account homeAddress(Address homeAddress) {
        setHomeAddress(homeAddress);
        return this;
    }

    /**
     * Equals implementation using a business key.
     */
    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Account && hashCode() == other.hashCode());
    }

    private volatile int previousHashCode = 0;

    @Override
    public int hashCode() {
        int hashCode = Objects.hashCode(getUsername());

        if (previousHashCode != 0 && previousHashCode != hashCode) {
            log.warn("DEVELOPER: hashCode has changed!." //
                    + "If you encounter this message you should take the time to carefully review equals/hashCode for: " //
                    + getClass().getCanonicalName());
        }

        previousHashCode = hashCode;
        return hashCode;
    }

    /**
     * Construct a readable string representation for this Account instance.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("id", getId()) //
                .add("username", getUsername()) //
                .add("birthDate", getBirthDate()) //
                .toString();
    }
}