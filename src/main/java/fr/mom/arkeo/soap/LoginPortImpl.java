
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package fr.mom.arkeo.soap;

import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.1.6
 * 2016-04-07T08:09:17.417+02:00
 * Generated source version: 3.1.6
 * 
 */

@javax.jws.WebService(
                      serviceName = "LoginService",
                      portName = "LoginPort",
                      targetNamespace = "http://soap.arkeo.mom.fr/",
                      wsdlLocation = "http://ark.mom.fr/arkeo/services/LoginPort?wsdl",
                      endpointInterface = "fr.mom.arkeo.soap.Login")
                      
public class LoginPortImpl implements Login {

    private static final Logger LOG = Logger.getLogger(LoginPortImpl.class.getName());

    /* (non-Javadoc)
     * @see fr.mom.arkeo.soap.Login#authentification(java.lang.String  arg0 ,)java.lang.String  arg1 ,)java.lang.String  arg2 )*
     */
    public fr.mom.arkeo.soap.Account authentification(java.lang.String arg0,java.lang.String arg1,java.lang.String arg2) { 
        LOG.info("Executing operation authentification");
        System.out.println(arg0);
        System.out.println(arg1);
        System.out.println(arg2);
        try {
            fr.mom.arkeo.soap.Account _return = new fr.mom.arkeo.soap.Account();
            _return.setBaseId("BaseId1017926138");
            java.util.List<fr.mom.arkeo.soap.Group> _returnGroups = new java.util.ArrayList<fr.mom.arkeo.soap.Group>();
            fr.mom.arkeo.soap.Group _returnGroupsVal1 = new fr.mom.arkeo.soap.Group();
            _returnGroupsVal1.setId("Id-376589052");
            _returnGroupsVal1.setName("Name-909738428");
            _returnGroups.add(_returnGroupsVal1);
            _return.getGroups().addAll(_returnGroups);
            fr.mom.arkeo.soap.User _returnUser = new fr.mom.arkeo.soap.User();
            _returnUser.setDbId(125475745);
            _returnUser.setFirstname("Firstname397194605");
            _returnUser.setLastname("Lastname1556846466");
            _returnUser.setMail("Mail2126738222");
            _returnUser.setUid("Uid608838457");
            _returnUser.setUser("User2113556063");
            fr.mom.arkeo.soap.Group _returnUserUserGroup = new fr.mom.arkeo.soap.Group();
            _returnUserUserGroup.setId("Id-1703594804");
            _returnUserUserGroup.setName("Name-1816279402");
            _returnUser.setUserGroup(_returnUserUserGroup);
            _return.setUser(_returnUser);
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
