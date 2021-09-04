package org.jpwh.test.simple;

import org.testng.annotations.Test;

public class CRUDMetadataHBMXML extends CRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimpleXMLHibernatePU", "simple/Native.hbm.xml");
    }

    @Test
    @Override
    public void storeAndQueryItems() throws Exception {
        super.storeAndQueryItems("findItemsHibernate");
    }
}
