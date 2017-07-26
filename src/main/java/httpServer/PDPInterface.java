package httpServer;

import org.wso2.balana.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPInterface {

    private static PDPInterface pdpInterface;
    PDP pdp;
    Balana balana;

    public static PDPInterface getInstance() {
        if (pdpInterface == null)
            pdpInterface = new PDPInterface();
        return pdpInterface;
    }

    private PDPInterface() {
        initBalana();
    }

    public String evaluate(String request, String pepId) {
        pdp = getPDPNewInstance(pepId);
        return pdp.evaluate(request);
    }


    private PDP getPDPNewInstance(String pepId) {

        reloadBalana(pepId,null,null);
        PDPConfig pdpConfig = balana.getPdpConfig();
        return new PDP(pdpConfig);
    }

    public boolean reloadBalana(String pdpConfigName, String attributeFactoryName, String functionFactoryName) {
        ConfigurationStore configurationStore = null;
        try {
            configurationStore = new ConfigurationStore();
            if (configurationStore != null) {
                if (pdpConfigName != null) {
                    balana.setPdpConfig(configurationStore.getPDPConfig(pdpConfigName));
                } else {
                    balana.setPdpConfig(configurationStore.getDefaultPDPConfig());
                }

                if(attributeFactoryName != null){
                    balana.setAttributeFactory(configurationStore.getAttributeFactory(attributeFactoryName));
                } else {
                    balana.setAttributeFactory(configurationStore.getDefaultAttributeFactoryProxy().getFactory());
                }

                if(functionFactoryName != null){
                    balana.setFunctionTargetFactory(configurationStore.getFunctionFactoryProxy(functionFactoryName).getTargetFactory());
                } else {
                    balana.setFunctionTargetFactory(configurationStore.getDefaultFunctionFactoryProxy().getTargetFactory());
                }

                if(functionFactoryName != null){
                    balana.setFunctionConditionFactory(configurationStore.getFunctionFactoryProxy(functionFactoryName).getConditionFactory());
                } else {
                    balana.setFunctionConditionFactory(configurationStore.getDefaultFunctionFactoryProxy().getConditionFactory());
                }

                if(functionFactoryName != null){
                    balana.setFunctionGeneralFactory(configurationStore.getFunctionFactoryProxy(functionFactoryName).getGeneralFactory());
                } else {
                    balana.setFunctionGeneralFactory(configurationStore.getDefaultFunctionFactoryProxy().getGeneralFactory());
                }

                if(functionFactoryName != null){
                    balana.setCombiningAlgFactory(configurationStore.getCombiningAlgFactory(functionFactoryName));
                } else {
                    balana.setCombiningAlgFactory(configurationStore.getDefaultCombiningFactoryProxy().getFactory());
                }
            }
            return true;
        } catch (ParsingException e) {
            e.printStackTrace();
            return false;
        } catch (UnknownIdentifierException e) {
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


}
