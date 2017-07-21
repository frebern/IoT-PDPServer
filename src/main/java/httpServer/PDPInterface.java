package httpServer;

import IntentConflictExample.SampleAttributeFinderModule;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.impl.CurrentEnvModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.SelectorModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPInterface {

    private static PDPInterface pdpInterface;
    PDP pdp;

    //Thread-safe singleton
    public static PDPInterface getInstance() {
        return pdpInterface = Singleton.instance;
    }
    private PDPInterface() {}
    private static class Singleton{
        private static final PDPInterface instance = new PDPInterface();
    }

    public String evaluate(String request, String ...policies) {
        pdp = getPDPNewInstance(policies);
        return pdp.evaluate(request);
    }


    private PDP getPDPNewInstance(String ...policies){

        PDPConfig pdpConfig = createConfig(policies);

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();
        finderModules.add(new SampleAttributeFinderModule());
        attributeFinder.setModules(finderModules);
        return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), null, true));
    }

    private PDPConfig createConfig(String ...policies) {

        PolicyFinder policyFinder = new PolicyFinder();
        HashSet policyFinderModules = new HashSet();
        HashSet<String> policyLocations = new HashSet<>();
        policyLocations.addAll(Arrays.asList(policies));

        // Set Policy Finder
        FileBasedPolicyFinderModule fileBasedPolicyFinderModule = new FileBasedPolicyFinderModule(policyLocations);
        policyFinderModules.add(fileBasedPolicyFinderModule);
        policyFinder.setModules(policyFinderModules);

        // Set Attribute Finder
        AttributeFinder attributeFinder = new AttributeFinder();
        ArrayList attributeFinderModules = new ArrayList();
        SelectorModule selectorModule = new SelectorModule();
        CurrentEnvModule currentEnvModule = new CurrentEnvModule();
        attributeFinderModules.add(selectorModule);
        attributeFinderModules.add(currentEnvModule);
        attributeFinder.setModules(attributeFinderModules);

        PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, null, false);
        return pdpConfig;
    }

}
