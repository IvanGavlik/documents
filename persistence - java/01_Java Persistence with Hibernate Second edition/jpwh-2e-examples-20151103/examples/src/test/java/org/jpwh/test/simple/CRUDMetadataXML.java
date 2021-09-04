package org.jpwh.test.simple;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CRUDMetadataXML extends CRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimpleXMLCompletePU");
    }

    @Test
    @Override
    public void storeAndQueryItems() throws Exception {
        super.storeAndQueryItems();
    }
}
