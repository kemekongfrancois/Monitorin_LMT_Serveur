/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws;

import clientWS.WSClientMonitoring;
import clientWS.WSClientMonitoringService;
import entite.Machine;
import entite.Tache;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import sessionBean.Bean;
import until.Until;

/**
 *
 * @author KEF10
 */
@WebService(serviceName = "WsMonitoring")
public class WsMonitoring {
    private final String DEFAUL_PERIODE_CHECK_MACHINE = "2 * * * * ?";//represente la valeur par defaut de la période de check des machine
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
       
        //resultat += "\ncreation de la machine"+creerMachine("172.16.4.2", "8088", "Windows", "KEF");
        //resultat += "\ncreation de la machine"+creerTacheSurveilleDD("172.16.4.2", "2 * * * * ?",80, "c");
        
        
       WSClientMonitoring ws = appelWSClient("172.16.4.2", "8088");
       resultat += "\n"+ ws.hello(PAUSE);
        
        return "Hello je suis le WSServeur " + txt + " !" + resultat ;
    }
    /**
     * cette fonction permet d'ouvrire une connection web service ver un client
     * @param adresse
     * @param port
     * @return 
     */
    private WSClientMonitoring appelWSClient(String adresse, String port){
        URL url = null;
        try {
            //String adresse = "172.16.4.2";
            //String port = "8088";
            url = new URL("http://" + adresse + ":" + port + "/WSClientMonitoring?wsdl");
        } catch (MalformedURLException ex) {
            Logger.getLogger(WsMonitoring.class.getName()).log(Level.SEVERE, null, ex);
            Until.savelog("Adresse du serveur ou port invalide \n"+ex, Until.fichieLog);
        }
        WSClientMonitoringService service = new WSClientMonitoringService(url);
        return service.getWSClientMonitoringPort();
    }
    /*
    @WebMethod
    //public String creerMachine(String AdresIP, String nonOS, String nomMachine) {
    public Machine creerMachine(@WebParam(name = "AdresIP") String AdresIP,@WebParam(name = "Port")String port, @WebParam(name = "nonOS") String nonOS, @WebParam(name = "nomMachine") String nomMachine){
        return bean.creerMachine(AdresIP, port, DEFAUL_PERIODE_CHECK_MACHINE, nonOS, nomMachine);
    }
    */
    @WebMethod
    public Machine verifiOuCreerMachine(
            @WebParam(name = "AdresIP") String AdresIP,
            @WebParam(name = "Port")String port, 
            @WebParam(name = "nonOS") String nonOS, 
            @WebParam(name = "nomMachine") String nomMachine){
        return bean.verifiOuCreerMachine(AdresIP,port, DEFAUL_PERIODE_CHECK_MACHINE, nonOS, nomMachine);
    }
    
    public Tache creerTacheSurveilleDD(
            @WebParam(name = "adresIpMachine") String adresIpMachine, 
            @WebParam(name = "periode") String periode,
            @WebParam(name = "seuil") int seuil,
            //@WebParam(name = "statue") String statue,
            @WebParam(name = "lettre_partition") String lettre_partition){
        return bean.creerTacheSurveilleDD(adresIpMachine, periode, lettre_partition,seuil, STOP);
    }
}
