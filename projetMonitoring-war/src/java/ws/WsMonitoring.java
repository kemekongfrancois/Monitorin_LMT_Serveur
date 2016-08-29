/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws;


import entite.Machine;
import entite.Tache;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import sessionBean.Bean;
import until.Until;

/*doit être ajouter au classe générer par le web service client 
affin que ces classe utilise les objets venant de "entite" et non ceux provenant du wed service

import entite.Tache;
import entite.TachePK;
import entite.Machine;
*/

/**
 *
 * @author KEF10
 */
@WebService(serviceName = "WsMonitoring")
public class WsMonitoring {
    

    @EJB
    private Bean bean;
    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        
        
       //WSClientMonitoring ws = appelWSClient(adressTest, "8088");
       //resultat += "\n l'autre "+ ws.hello(ALERTE);
       //Tache tacheBean = bean.getTache(1,"c:");
       //clientWS.Tache tache = convertiTacheBeanEnTacheClient(tacheBean);
       
       //resultat += "\n demarage de la tache DD"+ws.demarerMetAJourOUStopperTache(tacheBean);
        
        return "Hello je suis le WS Serveur " + txt + " ! version= 1.00";
    }
    
    /*@WebMethod
    public String initialisation(){
    return bean.initialisation();
    }
    */
   
    /*
    private clientWS.Tache convertiTacheBeanEnTacheClient(Tache tache){
        clientWS.Tache tacheClient = new clientWS.Tache();
        tacheClient.setDescriptionFichier(tache.getDescriptionFichier());
        tacheClient.setListeAdresse(tache.getListeAdresse());
        
       // tacheClient.setMachine(convertirMachineBeanEnMachineClient(tache.getMachineByIP()));
        
        tacheClient.setPeriodeVerrification(tache.getPeriodeVerrification());
        tacheClient.setSeuilAlerte(tache.getSeuilAlerte());
        tacheClient.setStatue(tache.getStatue());
        TachePK cle = new TachePK();
        
        cle.setCleTache(tache.getTachePK().getCleTache());
        cle.setIdMachine(tache.getTachePK().getIdMachine());
        tacheClient.setTachePK(cle);
        tacheClient.setTypeTache(tache.getTypeTache());
        
        return tacheClient;
    }
    
    private clientWS.Machine convertirMachineBeanEnMachineClient(Machine machine){
        clientWS.Machine machineClient = new clientWS.Machine();
        
        machineClient.setAdresseIP(machine.getAdresseIP());
        machineClient.setIdMachine(machine.getIdMachine());
        machineClient.setNomMachine(machine.getNomMachine());
        machineClient.setPeriodeDeCheck(machine.getPeriodeDeCheck());
        machineClient.setPortEcoute(machine.getPortEcoute());
        machineClient.setTypeOS(machine.getTypeOS());
        
        return machineClient;
    }
    */
    /*
    @WebMethod
    //public String creerMachine(String AdresIP, String nonOS, String nomMachine) {
    public Machine creerMachine(@WebParam(name = "AdresIP") String AdresIP,@WebParam(name = "Port")String port, @WebParam(name = "nonOS") String nonOS, @WebParam(name = "nomMachine") String nomMachine){
        return bean.creerMachine(AdresIP, port, DEFAUL_PERIODE_CHECK_MACHINE, nonOS, nomMachine);
    }
    */
    @WebMethod
    public Machine creerOuVerifiMachine(
            @WebParam(name = "AdresIP") String AdresIP,
            @WebParam(name = "Port")String port, 
            @WebParam(name = "nonOS") String nonOS, 
            @WebParam(name = "nomMachine") String nomMachine){
        return bean.creerOuVerifiMachine(AdresIP,port, Bean.DEFAUL_PERIODE_CHECK_MACHINE, nonOS, nomMachine);
    }
    
    @WebMethod
    public Machine getMachine(@WebParam(name = "AdresIP")String AdresIP){
        return bean.getMachineByIP(AdresIP);
    }
    
    @WebMethod
    public List<Tache> getListTacheMachine(@WebParam(name = "AdresIP")String ipAdresse){
        return bean.getListTacheMachine(ipAdresse);
    }
    
    @WebMethod
    public boolean traitementAlerteTache(
            @WebParam(name = "idTache")int idTache,
            @WebParam(name = "codeErreur")int codeErreur){
        
        return bean.traitementAlerteTache(idTache,codeErreur);
    }
    
    @WebMethod
    public boolean traitementAlerteMachine(
            @WebParam(name = "idMachine")int idMachine,
            @WebParam(name = "listTachePB")List<Integer> listTachePB){
        return bean.traitementAlerteMachine(idMachine, listTachePB);
    }
    
    @WebMethod
    public Tache getTache(@WebParam(name = "idMachine")int IdMachine) {
        return bean.getTache(IdMachine);
    }
    @WebMethod
    public boolean verifiNomTacheSurMachine(String adresIpMachine, String nomTache) {
        return bean.verifiNomTacheSurMachine(adresIpMachine,nomTache);
    }
    @WebMethod
    public boolean problemeTacheResolu(@WebParam(name = "idTache")int idTache) {
        return bean.problemeTacheResolu(idTache);
    }
    
    /*
    public Tache creerTacheSurveilleDD(
            @WebParam(name = "adresIpMachine") String adresIpMachine, 
            @WebParam(name = "periode") String periode,
            @WebParam(name = "seuil") int seuil,
            //@WebParam(name = "statue") String statue,
            @WebParam(name = "lettre_partition") String lettre_partition){
        return bean.creerTacheSurveilleDD(adresIpMachine, periode, lettre_partition,seuil, STOP);
    }
    */
}
