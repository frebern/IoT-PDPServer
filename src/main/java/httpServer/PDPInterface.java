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
import java.util.HashSet;
import java.util.List;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPInterface {

    private static PDPInterface pdpInterface;
    PDP pdp;

    public static PDPInterface getInstance() {
        if (pdpInterface == null)
            pdpInterface = new PDPInterface();
        return pdpInterface;
    }

    private PDPInterface() {
    }

    public String evaluate(String request, String ...policies) {
        pdp = getPDPNewInstance(policies);
        return pdp.evaluate(request);
    }

    private PDPConfig createConfig(String ...policies) {

        PolicyFinder policyFinder1 = new PolicyFinder();
        HashSet policyFinderModules1 = new HashSet();
        HashSet<String> policyLocations = new HashSet<>();
        for (String c : policies) {
            policyLocations.add(c);
        }
        FileBasedPolicyFinderModule fileBasedPolicyFinderModule = new FileBasedPolicyFinderModule(policyLocations);
        policyFinderModules1.add(fileBasedPolicyFinderModule);
        policyFinder1.setModules(policyFinderModules1);
        AttributeFinder attributeFinder = new AttributeFinder();
        ArrayList attributeFinderModules = new ArrayList();
        SelectorModule selectorModule = new SelectorModule();
        CurrentEnvModule currentEnvModule = new CurrentEnvModule();
        attributeFinderModules.add(selectorModule);
        attributeFinderModules.add(currentEnvModule);
        attributeFinder.setModules(attributeFinderModules);

        PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder1, (ResourceFinder)null, false);
        return pdpConfig;
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


}
