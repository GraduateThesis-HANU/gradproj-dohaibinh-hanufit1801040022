package com.hanu.courseman;

import com.hanu.courseman.modules.ModuleMain;
import com.hanu.courseman.modules.address.ModuleAddress;
import com.hanu.courseman.modules.coursemodule.ModuleCourseModule;
import com.hanu.courseman.modules.enrolment.ModuleEnrolment;
import com.hanu.courseman.modules.sclass.ModuleSClass;
import com.hanu.courseman.modules.student.ModuleStudent;
import domainapp.basics.core.dodm.osm.OSM;
import domainapp.basics.model.config.Configuration.Language;
import domainapp.basics.model.config.dodm.OsmConfig.ConnectionType;
import domainapp.core.dodm.dom.DOM;
import domainapp.core.dodm.dsm.DSM;
import domainapp.model.meta.app.*;
import domainapp.setup.SetUpConfig;

@SystemDesc(
        appName = "Courseman",
        splashScreenLogo = "coursemanapplogo.jpg",
        language = Language.English,
        orgDesc = @OrgDesc(name = "Faculty of IT",
                address = "K1m9 Nguyen Trai Street, Thanh Xuan District",
                logo = "hanu.gif",
                url = "http://localhost:5432/domains"),
        dsDesc = @DSDesc(
                type = "postgresql",
                dsUrl = "http://localhost:5432/domains",
                user = "admin",
                password = "password",
                dsmType = DSM.class,
                domType = DOM.class,
                osmType = OSM.class,
                connType = ConnectionType.Client),
        modules = {
                ModuleMain.class,
                ModuleCourseModule.class,
                ModuleEnrolment.class,
                ModuleStudent.class,
                ModuleAddress.class,
                ModuleSClass.class
        },
        sysModules = {},
        setUpDesc = @SysSetUpDesc(setUpConfigType = SetUpConfig.class),
        securityDesc = @SecurityDesc(isEnabled = false))
public class SCC1 {
}
