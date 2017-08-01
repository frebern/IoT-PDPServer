package httpServer;

import org.wso2.balana.*;
import org.wso2.balana.attr.AttributeFactory;
import org.wso2.balana.combine.CombiningAlgFactory;
import org.wso2.balana.cond.FunctionFactoryProxy;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.impl.CurrentEnvModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.SelectorModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class PDPInterface {

    private static PDPInterface pdpInterface;
    private PDP pdp;
    private Balana balana;

    //Thread-safe singleton
    public static PDPInterface getInstance() {
        return pdpInterface = Singleton.instance;
    }
    private PDPInterface() {
        initBalana();
    }
    private static class Singleton{
        private static final PDPInterface instance = new PDPInterface();
    }

    // API 1. evaluate
    public String evaluate(String request, String pepId) {
        // 매번 생성하는 로드를 줄이기 위한 방법을 검토해볼 것.
        // (예를들어, 현재 PDP와 pdpConfigName이 같다면 재활용 한다던지...
        // 단 같은 이름이어도 config.xml이 수정될수도 있으니 유의해야함)
        pdp = getPDPNewInstance(getPDPConfigName(pepId));
        return pdp.evaluate(request);
    }

    // 나중에 PEP ID를 통해 PDP Config 명을 조회하는 규칙 작성 필요.
    private String getPDPConfigName(String pepId) {
        return pepId;
    }

    private PDP getPDPNewInstance(String pdpConfigName) {
        reloadBalana(pdpConfigName, null, null);
        PDPConfig pdpConfig = balana.getPdpConfig();
        return new PDP(pdpConfig);
    }

    // API 2 ? (이 부분 API로 따야하는지?)
    public boolean reloadBalana(String pdpConfigName, String attributeFactoryName, String functionFactoryName) {
        try {
            ConfigurationStore configurationStore = new ConfigurationStore();
            if (configurationStore != null) {
                PDPConfig pdpConfig = pdpConfigName != null
                        ? configurationStore.getPDPConfig(pdpConfigName)
                        : configurationStore.getDefaultPDPConfig();

                AttributeFactory attributeFactory = attributeFactoryName != null
                        ? configurationStore.getAttributeFactory(attributeFactoryName)
                        : configurationStore.getDefaultAttributeFactoryProxy().getFactory();

                FunctionFactoryProxy proxy = functionFactoryName != null
                        ? configurationStore.getFunctionFactoryProxy(functionFactoryName)
                        : configurationStore.getDefaultFunctionFactoryProxy();

                CombiningAlgFactory combiningAlgFactory = functionFactoryName != null
                        ? configurationStore.getCombiningAlgFactory(functionFactoryName)
                        : configurationStore.getDefaultCombiningFactoryProxy().getFactory();

                balana.setPdpConfig(pdpConfig);
                balana.setAttributeFactory(attributeFactory);
                balana.setFunctionTargetFactory(proxy.getTargetFactory());
                balana.setFunctionConditionFactory(proxy.getConditionFactory());
                balana.setFunctionGeneralFactory(proxy.getGeneralFactory());
                balana.setCombiningAlgFactory(combiningAlgFactory);
            }
            return true;
        } catch (ParsingException | UnknownIdentifierException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initBalana(){
        try {
            String configLocation = (new File(".")).getCanonicalPath() + File.separator + "resources/config.xml";
            System.setProperty(ConfigurationStore.PDP_CONFIG_PROPERTY, configLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        balana = Balana.getInstance();
    }

    // 여기 필요함?
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
