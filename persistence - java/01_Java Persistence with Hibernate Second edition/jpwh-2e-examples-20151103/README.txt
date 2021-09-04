==============================================================================
Java Persistence with Hibernate - Second Edition

http://www.manning.com/bauer3/

==============================================================================


GETTING STARTED
------------------------------------------------------------------------------

- Install JDK 7.

- Install Maven 3.x.

- Run 'mvn clean test' to execute all examples (this will take a while if all
  dependencies have to be downloaded for the first time).

- Open the report in examples/target/surefire-reports/index.html

- Read more about the modules in each sub-directory's pom.xml file and browse
  the source code.

- To get more logging output, edit shared/src/main/resources/logging.properties
  and run the tests. All log output will be written to the file
  examples/target/surefire-reports/TestSuite-output.txt

- To run a single test only, first install the modules into your local Maven
  repository with 'mvn clean install'. Then run the test with:

      mvn -pl examples -Dtest=org.jpwh.test.simple.CRUD clean test

- If you only run a single test, the console log output will be written to
  a different file, in the previous case:
  examples/target/surefire-reports/org.jpwh.test.simple.CRUD-output.txt


RUNNING EXAMPLE APPS
------------------------------------------------------------------------------

- Install Wildfly 8.2.0.Final

- Run the application server with $WILDFLY/bin/standalone.sh in the background

- Run the "Stateless Client/Server" example app:

    mvn -P app-stateless-server clean install
    mvn -P app-stateless-server clean package wildfly:deploy
    mvn -P app-stateless-client clean test
    mvn -P app-stateless-server clean package wildfly:undeploy

- Run the "Stateful Client/Server" example app:

    mvn -P app-stateful-server clean install
    mvn -P app-stateful-server clean package wildfly:deploy
    mvn -P app-stateful-client clean test
    mvn -P app-stateful-server clean package wildfly:undeploy

- Run the "CaveatEmptor Web Application" example:

    mvn -P app-web clean package wildfly:deploy
    Open in browser: http://localhost:8080/app-web/
    mvn -P app-web wildfly:undeploy


RUNNING WITH/BROWSING AN EXTERNAL DATABASE
------------------------------------------------------------------------------

- An in-memory H2 database instance is used for tests by default. You can't
  access and browse this database with an SQL console. If you only want to see
  the schema/SQL, edit logging.properties as described above.

- You can switch to an external already running H2 database. First start the
  database by double-clicking on the H2.jar file. It should be in your Maven
  repository (~/.m2/repository/com/h2database/) or you can download it from
  http://h2database.com. The H2 web console will open and you can simply
  connect to the database instance with user 'sa' and no password.

- Install the example modules into your local repository:

    mvn clean install

- Run a single test method and keep the schema/data after completion with:

    mvn -pl examples \
     -Dtest=org.jpwh.test.simple.CRUD#storeAndQueryItems \
     -DconnectionURL=jdbc:h2:tcp://localhost/mem:test \
     -DkeepSchema=true \
     clean test

- Browse the database after the test method executes in the H2 console.

- You can execute the same test method again, the database will be cleaned
  before the method runs. You can execute other methods of the same test
  class, each test class uses the same database schema.

- To execute methods of another test class with a different schema, stop
  and start the H2 database when switching test classes. Note that H2
  deletes an in-memory database when the last connection is dropped. If
  you disconnect in the H2 web console, the database will be deleted.


------------------------------------------------------------------------------

Visit us on the Manning author forum:
    http://www.manning-sandbox.com/forum.jspa?forumID=844
