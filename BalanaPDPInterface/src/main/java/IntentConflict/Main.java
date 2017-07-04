/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package IntentConflict;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.xacml3.Advice;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Ride Car sample
 */
public class Main {

    private static Balana balana;

    public static void main(String[] args){

    	Scanner console;
        String who = "";
        String assist = "";
        int hour = 0;
        int min = 0;

        //import policy resource (.\resources)
        initBalana();

        //Input Params (userName, productId, productAmount)
        if ((console = new Scanner(System.in)) != null){
        	System.out.print("Who want to ride car? (father:fred, mother:monica, son:sam, daughter:diana) : ");
            who = console.nextLine();
            console.close();
            if(who == null || who.trim().length() < 1 ){
                System.err.println("\nInput can not be empty\n");
                return;
            }

            System.out.print("Who sit in the passenger seat? (father:fred, mother:monica, son:sam, daughter:diana, no passenger:none) : ");
            assist = console.nextLine();
            if(assist == null || assist.trim().length() < 1 ){
                System.err.println("\nInput can not be empty\n");
                return;
            }
            
            System.out.print("Enter hour (0~23) : ");
            String time = console.nextLine();
            if(assist == null || assist.trim().length() < 1 ){
                System.err.println("\nInput can not be empty\n");
                return;
            }
            
        	try{
        		hour = Integer.parseInt(time);
        		if(hour<0 || hour>=24){
        			System.err.println("\nInvalid hour range (0~23)\n");
        			return;
        		}
        	}catch(NumberFormatException e){
        		System.err.println("\nHour must be integer\n");
        		return;
        	}
            
        }
        

        
        String request = createXACMLRequest(who, assist, hour, min);
        //String request = createXACMLRequest("son", "father", 14, 40);
        //Get PDP instance
        PDP pdp = getPDPNewInstance();

        System.out.println("\n======================== XACML Request ====================");
        System.out.println(request);
        System.out.println("===========================================================");

        //Evaluate request & get response from PDP
        String response = pdp.evaluate(request);

        System.out.println("\n======================== XACML Response ===================");
        System.out.println(response);
        System.out.println("===========================================================");
        
        try {
            ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(response));
            AbstractResult result  = responseCtx.getResults().iterator().next();
            if(AbstractResult.DECISION_PERMIT == result.getDecision()){
                System.err.println("\n" + who + " is authorized to perform this start\n\n");
            } else {
                System.err.println("\n" + who + " is NOT authorized to perform this start\n");
            }
            List<Advice> advices = result.getAdvices();
            for(Advice advice : advices){
                List<AttributeAssignment> assignments = advice.getAssignments();
                for(AttributeAssignment assignment : assignments){
                    System.out.print("Advice :  " + assignment.getContent() +"\n\n");
                }
            }
        } catch (ParsingException e) {
            e.printStackTrace();
        }

    }

    private static void initBalana(){

        try{
            // using file based policy repository. so set the policy location as system property
            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        balana = Balana.getInstance();
    }

    /**
     * Returns a new PDP instance with new XACML policies
     *
     * @return a  PDP instance
     */
    private static PDP getPDPNewInstance(){

        PDPConfig pdpConfig = balana.getPdpConfig();

        // registering new attribute finder. so default PDPConfig is needed to change
        AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
        List<AttributeFinderModule> finderModules = attributeFinder.getModules();
        finderModules.add(new SampleAttributeFinderModule());
        attributeFinder.setModules(finderModules);

        return new PDP(new PDPConfig(attributeFinder, pdpConfig.getPolicyFinder(), null, true));
    }

    /**
     * Creates DOM representation of the XACML request
     *
     * @param response  XACML request as a String object
     * @return XACML request as a DOM element
     */
    public static Element getXacmlResponse(String response) {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory dbf;
        Document doc;

        inputStream = new ByteArrayInputStream(response.getBytes());
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            doc = dbf.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            System.err.println("DOM of request element can not be created from String");
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
               System.err.println("Error in closing input stream of XACML response");
            }
        }
        return doc.getDocumentElement();
    }    

    public static String createXACMLRequest(String who, String assist, int hour, int min){
    	
        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
        			/* Action (start)*/
                	"<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                		"<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                			"<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + "start" + "</AttributeValue>\n" +
                		"</Attribute>\n" +
                	"</Attributes>\n" +
                	/* Subject (who) */
                	"<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                		"<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                			"<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + who +"</AttributeValue>\n" +
                		"</Attribute>\n" +
                	"</Attributes>\n" +
                	/* Resource (car) */
                	"<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
            			"<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
            				"<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + "car" + "</AttributeValue>\n" +
            			"</Attribute>\n" +
            		"</Attributes>\n" +
            		/* Environments (assist, hour) */
                	"<Attributes Category=\"http://selab.hanyang.ac.kr/category\">\n" +
                		"<Attribute AttributeId=\"http://selab.hanyang.ac.kr/id/assist\" IncludeInResult=\"false\">\n" +
        					"<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + assist + "</AttributeValue>\n" +
        				"</Attribute>\n" +
                		"<Attribute AttributeId=\"http://selab.hanyang.ac.kr/id/hour\" IncludeInResult=\"false\">\n" +
                			"<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + hour + "</AttributeValue>\n" +
                		"</Attribute>\n" +
                	"</Attributes>\n" +
                "</Request>";

    }
}
