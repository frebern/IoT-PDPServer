package httpServer;

import IntentConflictExample.SampleAttributeFinderModule;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPInterface {

    private static PDPInterface pdpInterface;
    Balana balana;
    PDP pdp;

    public static PDPInterface getInstance() {
        if (pdpInterface == null)
            pdpInterface = new PDPInterface();
        return pdpInterface;
    }

    private PDPInterface() {
        initBalana();
        pdp = getPDPNewInstance();
    }

    public String evaluate(String request) {
        return pdp.evaluate(request);
    }

    private void initBalana(){
        //TODO: Database로 변경 필요
        try{
            // using file based policy repository. so set the policy location as system property
            String sep = File.separator;
            String policyLocation = (new File(".")).getCanonicalPath() + sep + "resources" + sep + "IntentConflictExamplePolicies";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        balana = Balana.getInstance();
    }

    private PDP getPDPNewInstance(){

        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();
        finderModules.add(new SampleAttributeFinderModule());
        attributeFinder.setModules(finderModules);
        return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), null, true));
    }
}
