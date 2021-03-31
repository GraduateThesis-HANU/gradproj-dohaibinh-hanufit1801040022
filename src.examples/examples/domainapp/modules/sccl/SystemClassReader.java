package examples.domainapp.modules.sccl;

import java.util.Arrays;

import domainapp.basics.model.config.Configuration;
import domainapp.basics.model.config.dodm.DODMConfig;
import domainapp.basics.util.ApplicationToolKit;
import vn.com.courseman.software.SCC1;

/**
 * @overview 
 *
 * @author Duc Minh Le (ducmle)
 *
 * @version 
 */
public class SystemClassReader {
  public static void main(String[] args) {
    Class sysClass = SCC1.class;
    
    // initial config
    Configuration initCfg = ApplicationToolKit.parseInitApplicationConfiguration(sysClass);
    System.out.printf("Initial config: %n%s%n", initCfg);
    
    // data source config
    DODMConfig dodmCfg = initCfg.getDodmConfig();
    System.out.printf("%nData source config: %n%s%nJDBC URL: %s%nData source: %s%n", 
        dodmCfg,
        dodmCfg.getProtocolSpec(),
        dodmCfg.getOsmConfig().getDataSourceName() // more specific JDBC properties
        );
    
    // more complete config
    Configuration config = ApplicationToolKit.parseApplicationConfiguration(sysClass);
    System.out.printf("%nConfig: %n%s%n", config);
    System.out.printf("%nOrganisation: %n%s%n", config.getOrganisation());
    
    Class[] modules = ApplicationToolKit.parseApplicationModules(sysClass);
    System.out.printf("%nModules: %s%n", Arrays.toString(modules));
  }
}
