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

    public boolean activerDesactiveAlertSMS(@WebParam(name = "statut")boolean statut) {
        return bean.activerDesactiveAlertSMS(statut);
    }

    public boolean activerDesactiveAlertMail(@WebParam(name = "statut")boolean statut) {
        return bean.activerDesactiveAlertMail(statut);
    }

    
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
        
        return "Hello je suis le WSServeur " + txt + " !";
    }
    
    @WebMethod
    public String initialisation(){
        return bean.initialisation();
    }
    
    @WebMethod
    public boolean redemarerTache(@WebParam(name = "idTache")int idTache) {
        return bean.redemarerTache(idTache);
    }
   
    /*
    private clientWS.Tache convertiTacheBeanEnTacheClient(Tache tache){
        clientWS.Tache tacheClient = new clientWS.Tache();
        tacheClient.setDescriptionFichier(tache.getDescriptionFichier());
        tacheClient.setListeAdresse(tache.getListeAdresse());
        
       // tacheClient.setMachine(convertirMachineBeanEnMachineClient(tache.getMachine()));
        
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
    public Machine verifiOuCreerMachine(
            @WebParam(name = "AdresIP") String AdresIP,
            @WebParam(name = "Port")String port, 
            @WebParam(name = "nonOS") String nonOS, 
            @WebParam(name = "nomMachine") String nomMachine){
        return bean.verifiOuCreerMachine(AdresIP,port, Bean.DEFAUL_PERIODE_CHECK_MACHINE, nonOS, nomMachine);
    }
    
    @WebMethod
    public Machine getMachine(@WebParam(name = "AdresIP")String AdresIP){
        return bean.getMachine(AdresIP);
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
            @WebParam(name = "listTachePB")List<Tache> listTachePB){
        return bean.traitementAlerteMachine(idMachine, listTachePB);
    }
    
    @WebMethod
    public boolean pinger(@WebParam(name = "adres")String adres, @WebParam(name = "nbTentative")int nbTentative) {
        return bean.pinger(adres, nbTentative);
    }
    
    @WebMethod
    public String changerStatueMachine(@WebParam(name = "idMachine")Integer idMachine, @WebParam(name = "statue")boolean statue){
        return bean.changerStatueMachine(idMachine,statue);
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
