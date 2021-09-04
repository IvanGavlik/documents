package org.jpwh.web.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import static org.jpwh.Constants.API_VERSION;

@ApplicationPath(API_VERSION) // You should version your Web API
public class CaveatEmptorApplication extends Application {
}
