package com.demo.businessservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.3.6.fuse-780029-redhat-00001
 * 2021-07-12T16:44:54.740+02:00
 * Generated source version: 3.3.6.fuse-780029-redhat-00001
 *
 */
@WebService(targetNamespace = "http://www.demo.com/businessService", name = "personPortType")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface PersonPortType {

    @WebMethod
    @WebResult(name = "person", targetNamespace = "http://www.demo.com/businessService", partName = "parameters")
    public Person getPersonOp(

        @WebParam(partName = "parameters", name = "queryPerson", targetNamespace = "http://www.demo.com/businessService")
        QueryPerson parameters
    );
}
