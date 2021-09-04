package org.jpwh.shared.util;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

import java.util.List;

public class DatabaseTestMethodSelector implements IMethodSelector {

    @Override
    public void setTestMethods(List<ITestNGMethod> testMethods) {
    }

    @Override
    public boolean includeMethod(IMethodSelectorContext context, ITestNGMethod tm, boolean isTestMethod) {
        String[] groups = tm.getGroups();

        if (groups == null || groups.length == 0)
            return true;

        String database = System.getProperty("database");

        if (database == null && contains(groups, "H2")) {
            return true;
        }

        if (database != null && contains(groups, database)) {
            return true;
        }

        context.setStopped(true);
        return false;
    }

    protected boolean contains(String[] strings, String s) {
        for (String string : strings) {
            if (string.equals(s))
                return true;
        }
        return false;
    }
}
