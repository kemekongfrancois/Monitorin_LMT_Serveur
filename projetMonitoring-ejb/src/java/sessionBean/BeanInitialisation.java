/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Startup;

/**
 *
 * @author KEF10
 */
@Singleton
@LocalBean
@Startup
public class BeanInitialisation {

    @EJB
    private Bean bean;

    @Schedule(minute = "*/5", hour = "*")//cette tache vas s'exécuté toute les 5 minute
    public void verifieAgent() {
        System.out.println("verrification du fonctionnement des agents: " + new Date());
        List<Machine> listMAchineAvecStatue = bean.getAllMachineAvecBonStatue();
        for (Machine machine : listMAchineAvecStatue) {
            if (machine.getStatue().equals(Bean.START) || machine.getStatue().equals(Bean.STOP) || machine.getStatue().equals(Bean.ALERTE)) {
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le statue de: <<" + machine.getAdresseIP() + ">> es OK. statue= " + machine.getStatue());
            } else if (machine.getStatue().equals(Bean.ALERTE)) {
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le message d'alerte à déja été envoyé pour la machine: <<" + machine.getAdresseIP() + ">>");
            } else {
                String msg = "le statue de: <<" + machine.getAdresseIP() + ">> n'es pas bon!!!. statue= " + machine.getStatue();
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.SEVERE, msg);
                if (bean.envoiMessageAlerte("Alerte sur une des machines supervisé", msg)) {//on envoie le msg d'alerte et on met le statue à alerte
                    machine.setStatue(Bean.ALERTE);
                    bean.updateMachie(machine);
                }

            }
        }
    }

    @PostConstruct
    public void initialisation() {
        System.out.println("initialisation de la BD");

        int SEUIL_ALERT_DD = 90;
        String periodecheckPing = " 1 * * * * ?";
        int NB_TENTATIVE_PING = 10;

        String adressTest = "172.16.4.2";
        String periodecheckDD = " 1,30 * * * * ?";
        String periodecheckProcessus = " 10,40 * * * * ?";
        String periodecheckService = " 15,45 * * * * ?";
        String periodecheckFichierExistant = " 21,51 * * * * ?";
        String periodecheckFichierTaille = " 25,55 * * * * ?";
        String periodecheckTelnet = " 29,59 * * * * ?";
        String periodecheckDateModif = " 35,5 * * * * ?";
        int tailleMaxFichie = 5;
        int tailleMinFichie = -5;
        int seuilDateModif = 10;
        String portEcoute = "9039";

        String resultat = "";

        resultat += "\ninitialisation du serveur :-> " + bean.creerOuModifierServeur("monitoringlmtgroupe@gmail.com", "kefmonitoring", "testali", "OnAEyotL", "Alert LMT", false, false);
        //resultat += "\ncreation de la machine qui sera situe sur le serveur :-> " + creerMachine(ADRESSE_MACHINE_SERVEUR, portEcoute, DEFAUL_PERIODE_CHECK_MACHINE, OSWINDOWS, "machine Serveur");

        resultat += "\ncreation du 1er utilisateur :-> " + bean.creerUtilisateur("kef", "0000", "kemekong", "francois", "supAdmin", "237699667694", "kemekongfrancois@gmail.com");
        resultat += "\ncreation du 2ième utilisateur :-> " + bean.creerUtilisateur("kef2", "0000", "kemekong2", "francois2", "supAdmin", "237675954517", "kemekongfranois@yahoo.fr");

        resultat += "\ncreation de la machine :-> " + bean.creerMachine(adressTest, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF");

        resultat += "\ncreation de la tache DD :-> " + bean.creerTacheSurveilleDD(adressTest, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true,"");
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adressTest, periodecheckProcessus, "vlc.exe", Bean.START, true, false,"");
        resultat += "\ncreation de la tache Service :-> " + bean.creerTacheSurveilleService(adressTest, periodecheckService, "Connectify", Bean.START, true, true, true,"");
        resultat += "\ncreation de la tache Ping :-> " + bean.creerTachePing(adressTest, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache Ping 2 :-> " + bean.creerTachePing(adressTest, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier existant :-> " + bean.creerTacheSurveilleFichierExist(adressTest, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier superieur :-> " + bean.creerTacheSurveilleTailleFichier(adressTest, periodecheckFichierTaille, "c:/testMonitoring/Setup_Oscillo.exe", tailleMaxFichie, Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier inférieur :-> " + bean.creerTacheSurveilleTailleFichier(adressTest, periodecheckFichierTaille, "c:/testMonitoring/TeamViewer_Setup_fr.exe", tailleMinFichie, Bean.START, true, true,"");
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adressTest, periodecheckTelnet, "41.204.94.29", 8282, Bean.START, true, true,"");
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adressTest, periodecheckDateModif, "C:/testMonitoring/test date modification", seuilDateModif, Bean.START, true, true,"");

        String adressTest2 = "172.16.4.20";
        resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest2, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF virtuel");

        resultat += "\ncreation de la tache DD 2:-> " + bean.creerTacheSurveilleDD(adressTest2, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true,"");
        resultat += "\ncreation de la tache processus 2 :-> " + bean.creerTacheSurveilleProcessus(adressTest2, periodecheckProcessus, "vlc.exe", Bean.START, true, false,"");
        resultat += "\ncreation de la tache Service 2 :-> " + bean.creerTacheSurveilleService(adressTest2, periodecheckService, "HUAWEIWiMAX", Bean.START, true, true, true,"");
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adressTest2, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adressTest2, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier existant 2 :-> " + bean.creerTacheSurveilleFichierExist(adressTest2, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true,"");

        String adressTest3 = "172.16.4.22";
        resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest3, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF virtuel");

        resultat += "\ncreation de la tache DD 3:-> " + bean.creerTacheSurveilleDD(adressTest3, periodecheckDD, "/mnt/hgfs", SEUIL_ALERT_DD, Bean.START, true, true,"");
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adressTest3, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adressTest3, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier existant 3 :-> " + bean.creerTacheSurveilleFichierExist(adressTest3, periodecheckFichierExistant, "/home/ubuntu/Desktop/dist/parametre.txt", Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier superieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adressTest3, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/log.txt", tailleMaxFichie, Bean.START, true, true,"");
        resultat += "\ncreation de la tache fichier inférieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adressTest3, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/---keen'v - DIS MOI OUI (MARINA) Clip Officiel - YouTube.flv", tailleMinFichie, Bean.START, true, true,"");
        // resultat += "\ncreation de la tache processus 3 :-> " + bean.creerTacheSurveilleProcessus(adressTest3, periodecheckProcessus, "vlc.exe", START, false);
        //resultat += "\ncreation de la tache Service 3 :-> " + bean.creerTacheSurveilleService(adressTest3, periodecheckService, "HUAWEIWiMAX", START, true, true);

        String adressTestLMT83 = "192.168.100.83";
        resultat += "\ncreation de la tache DD sur " + adressTestLMT83 + ":-> " + bean.creerTacheSurveilleDD(adressTestLMT83, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true,"");

        System.out.println(resultat);
        //return resultat;
    }

}
