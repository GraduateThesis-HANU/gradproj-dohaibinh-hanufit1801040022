package vn.com.courseman.software;

import domainapp.basics.core.dodm.osm.OSM;
import domainapp.basics.model.config.Configuration.Language;
import domainapp.basics.model.config.dodm.OsmConfig.ConnectionType;
import domainapp.core.dodm.dom.DOM;
import domainapp.core.dodm.dsm.DSM;
import domainapp.model.meta.app.DSDesc;
import domainapp.model.meta.app.OrgDesc;
import domainapp.model.meta.app.SecurityDesc;
import domainapp.model.meta.app.SysSetUpDesc;
import domainapp.model.meta.app.SystemDesc;
import domainapp.setup.SetUpConfig;
import vn.com.courseman.modules.simple.ModuleMain;
import vn.com.courseman.modules.simple.address.ModuleAddress;

@SystemDesc(appName = "Courseman", splashScreenLogo = "coursemanapplogo.jpg", language = Language.English, orgDesc = @OrgDesc(name = "Faculty of IT", address = "K1m9 Nguyen Trai Street, Thanh Xuan District", logo = "hanu.gif", url = "http://localhost:5432/domains"), dsDesc = @DSDesc(type = "postgresql", dsUrl = "http://localhost:5432/domains", user = "admin", password = "password", dsmType = DSM.class, domType = DOM.class, osmType = OSM.class, connType = ConnectionType.Client), 
modules = { ModuleMain.class, ModuleAddress.class }, 
sysModules = {}, setUpDesc = @SysSetUpDesc(setUpConfigType = SetUpConfig.class), 
securityDesc = @SecurityDesc(isEnabled = false))
public class SCC1 {
}
