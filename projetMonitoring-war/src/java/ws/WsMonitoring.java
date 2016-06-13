/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws;

import entite.Machine;
import entite.SurveillerDd;
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
    private int periodeCheckMachine = 10;
    private final String PAUSE = "PAUSE";
    private final String START = "START";
    private final String STOP = "STOP";

    @EJB
    private Bean bean;

    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        String resultat = "";
        resultat += "\ncreation de la machine"+creerMachine("172.16.4.2", "8088", "Windows", "KEF");
        
        resultat += "\ncreation de la machine"+creerTacheSurveilleDD("172.16.4.2", 3,80, "c");
        
        
        return "Hello " + txt + " !" + resultat;
    }
    
    @WebMethod
    //public String creerMachine(String AdresIP, String nonOS, String nomMachine) {
    public Machine creerMachine(@WebParam(name = "AdresIP") String AdresIP,@WebParam(name = "Port")String port, @WebParam(name = "nonOS") String nonOS, @WebParam(name = "nomMachine") String nomMachine){
        return bean.creerMachine(AdresIP, port, periodeCheckMachine, nonOS, nomMachine);
    }
    
    @WebMethod
    public Machine verifiOuCreerMachine(
            @WebParam(name = "AdresIP") String AdresIP,
            @WebParam(name = "Port")String port, 
            @WebParam(name = "nonOS") String nonOS, 
            @WebParam(name = "nomMachine") String nomMachine){
        return bean.verifiOuCreerMachine(AdresIP,port, periodeCheckMachine, nonOS, nomMachine);
    }
    
    public SurveillerDd creerTacheSurveilleDD(
            @WebParam(name = "adresIpMachine") String adresIpMachine, 
            @WebParam(name = "periode") int periode,
            @WebParam(name = "seuil") int seuil,
            //@WebParam(name = "statue") String statue,
            @WebParam(name = "lettre_partition") String lettre_partition){
        return bean.creerTacheSurveilleDD(adresIpMachine, periode, lettre_partition,seuil, STOP);
    }
}
