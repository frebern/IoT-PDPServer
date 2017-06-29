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

import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sample attribute finder module. Actually this must be the point that calls to K-Market user store
 * and retrieve the customer attributes.  But here we are not talking any user store and values has
 * been hard corded in the source.
 */
public class SampleAttributeFinderModule extends AttributeFinderModule{

    private URI defaultSubjectId;

    public SampleAttributeFinderModule() {

        try {
            defaultSubjectId = new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }

    @Override
    public Set getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("http://selab.hanayng.ac.kr/id/role");
        return ids;   
    }

    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                                            URI category, EvaluationCtx context) {
    	
//    	System.out.println();
//    	System.out.println("attributeType: "+attributeType.toString());
//    	System.out.println("attributeId: "+attributeId.toString());
//    	System.out.println("issuer: "+issuer);
//    	System.out.println("category: "+category);
    	
    	
        String roleName = null;
        List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

        EvaluationResult result = context.getAttribute(attributeType, defaultSubjectId, issuer, category);
        if(result != null && result.getAttributeValue() != null && result.getAttributeValue().isBag()){
            BagAttribute bagAttribute = (BagAttribute) result.getAttributeValue();
            if(bagAttribute.size() > 0){
                String userName = ((AttributeValue) bagAttribute.iterator().next()).encode();
//                System.out.println("userName: "+userName);
                roleName = findRole(userName);
            }
        }

        if (roleName != null) {
        	//아들 bob은 가족이면서도, father, mother, son, daughter이라는 두개의 역할을 가질 수 있다.
        	attributeValues.add(new StringAttribute("family"));
            attributeValues.add(new StringAttribute(roleName));
        }

        return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
    }

    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

    private String findRole(String userName){

    	//father: fred, mother: monica, son: sam, daughter: diana
        if(userName.equals("fred")){
            return "father";
        } else if(userName.equals("monica")){
            return "mother";
        } else if(userName.equals("sam")){
            return "son";
        } else if(userName.equals("diana")){
            return "daughter";
        }
        return null;
    }
}
