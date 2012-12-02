## Query By Example for JPA 2

This API allows you to construct Query By Example using the JPA 2 Criteria API.4.

Note that the API does not depend on spring framework. Only the integration tests does.
However, the API has a small dependency on Hibernate 4.

The source is the doc. The best way to understand how it works is to study our integration tests. Just import the project into your favorite IDE and run the tests... It should work out of the box. It uses an embedded H2 database.

Here is the SQL file: [create.sql](https://github.com/jaxio/jpa-query-by-example/blob/master/src/test/resources)

Here are the entities: [create.sql](https://github.com/jaxio/jpa-query-by-example/blob/master/src/test/java/org/querybyexample/jpa/app)

Here are the associated Integration tests: [AccountQueryByExampleIT](https://github.com/jaxio/jpa-query-by-example/blob/master/src/test/java/org/querybyexample/jpa/it/AccountQueryByExampleIT.java)

Please send us your feedbacks or fork!

Provided by the Jaxio/SpringFuse team

* http://www.jaxio.com
* http://www.springfuse.com
* [@springfuse](https://twitter.com/springfuse)
