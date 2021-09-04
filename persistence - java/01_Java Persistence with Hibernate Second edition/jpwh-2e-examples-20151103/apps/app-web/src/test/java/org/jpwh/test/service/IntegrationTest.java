package org.jpwh.test.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jpwh.Constants;
import org.jpwh.shared.ImprovedH2Dialect;
import org.jpwh.web.dao.GenericDAO;
import org.jpwh.web.jaxrs.CaveatEmptorApplication;
import org.jpwh.web.jsf.CatalogService;
import org.jpwh.web.model.Item;

public class IntegrationTest extends Arquillian {

    @Deployment
    public static WebArchive getArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")

            .addPackage(IntegrationTest.class.getPackage())

            .addClass(Constants.class)

            .addPackage(CaveatEmptorApplication.class.getPackage())
            .addPackage(CatalogService.class.getPackage())
            .addPackage(Item.class.getPackage())
            .addPackage(GenericDAO.class.getPackage())
            .addPackage(ImprovedH2Dialect.class.getPackage())

            .addAsResource("META-INF/persistence.xml")
            .addAsResource("META-INF/orm.xml")
            .addAsResource("TestData.sql.txt")

            // MAKE SURE YOU EXECUTE THIS WITH THE MODULE DIRECTORY AS
            // THE CURRENT WORKING DIRECTORY OR IT WILL USE THE WRONG POM.XML!
            .addAsLibraries(Maven.resolver() // Why isn't there a Maven.resolveDependencies() with default settings?
                .loadPomFromFile("pom.xml") // And that isn't the default why exactly?
                .importRuntimeDependencies() // And this isn't the default why exactly?
                .resolve()  // OK, why would I want them NOT resolved?
                .withTransitivity() // You mean like this is the default everywhere else?
                .asFile()) // Sure, no idea why you can't figure this out by yourself...

            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
