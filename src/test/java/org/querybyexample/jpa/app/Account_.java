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

import java.util.Date;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Account.class)
public abstract class Account_ {

    // Raw attributes
    public static volatile SingularAttribute<Account, String> id;
    public static volatile SingularAttribute<Account, String> username;
    public static volatile SingularAttribute<Account, String> password;
    public static volatile SingularAttribute<Account, String> email;
    public static volatile SingularAttribute<Account, Integer> favoriteNumber;
    public static volatile SingularAttribute<Account, Boolean> isEnabled;
    public static volatile SingularAttribute<Account, Date> birthDate;

    // Technical attributes for query by example
    public static volatile SingularAttribute<Account, Integer> addressId;

    // One to one
    public static volatile SingularAttribute<Account, Legacy> legacy;
    
    // Many to one
    public static volatile SingularAttribute<Account, Address> homeAddress;

    // Many to many
    public static volatile ListAttribute<Account, Role> roles;
}