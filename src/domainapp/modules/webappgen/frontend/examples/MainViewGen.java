package domainapp.modules.webappgen.frontend.examples;

import domainapp.modules.webappgen.frontend.SC1;
import domainapp.modules.webappgen.frontend.examples.modules.ModuleMain;
import domainapp.modules.webappgen.frontend.generators.MainViewGenerator;
import domainapp.modules.webappgen.frontend.generators.utils.MCCUtils;
import domainapp.modules.mccl.model.MCC;

public class MainViewGen {
    public static void main(String[] args) {
        Class sysClass = SC1.class;
        MCC mainMCC = MCCUtils.readMCC(null, ModuleMain.class);

        System.out.println(new MainViewGenerator(sysClass, mainMCC).generate());
    }
}
