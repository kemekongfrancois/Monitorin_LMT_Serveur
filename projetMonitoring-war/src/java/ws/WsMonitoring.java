/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws;

import entite.Machine;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import sessionBean.Bean;

/**
 *
 * @author KEF10
 */
@WebService(serviceName = "WsMonitoring")
public class WsMonitoring {
    private int periodeCheck = 5;

    @EJB
    private Bean bean;

    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
       
        return "Hello " + txt + " !";
    }
    
    @WebMethod
    //public String creerMachine(String AdresIP, String nonOS, String nomMachine) {
    public String creerMachine(@WebParam(name = "AdresIP") String AdresIP, @WebParam(name = "nonOS") String nonOS, @WebParam(name = "nomMachine") String nomMachine){
        return bean.creerMachine(AdresIP, periodeCheck, nonOS, nomMachine);
    }
    
    @WebMethod
    public Machine verifiOuCreerMachine(@WebParam(name = "AdresIP") String AdresIP, @WebParam(name = "nonOS") String nonOS, @WebParam(name = "nomMachine") String nomMachine){
        return bean.verifiOuCreerMachine(AdresIP, periodeCheck, nonOS, nomMachine);
    }
}
