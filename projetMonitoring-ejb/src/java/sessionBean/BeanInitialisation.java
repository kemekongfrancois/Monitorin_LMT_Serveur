/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import java.util.ArrayList;
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

    public static List<String> listOS;
    public static List<String> listTypeTache;
    public static List<String> listTypeCompte;
    public static List<String> listTypeStatue;

    /**
     * verrifie la disponibilité des agents et met à jour le statu dans le cas
     * où la machine es de nouveau accessible
     *
     */
    @Schedule(minute = "*/5", hour = "*")//cette tache vas s'exécuté toute les 5 minute
    //@Schedule(second = "0", minute = "*", hour = "*")
    public void verifieEtUpdateAgent() {
        String sujet, msg;
        System.out.println("verrification du fonctionnement des agents: " + new Date());
        List<Machine> listMAchineAvecStatue = bean.getAllMachineAvecBonStatue();
        for (Machine machine : listMAchineAvecStatue) {
            Machine machineBD = bean.getMachineByIP(machine.getAdresseIP());//on recupére la machine réel en BD avec le statue
            if (machineBD.getStatue().equals(Bean.ALERTE)) {//on verrifie si on avait déja envoyer cette alerte
                if (machine.getStatue().equals(Bean.START)) {//la machine es de nouveau accessible
                    sujet = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                    msg = sujet;
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
                    if (bean.envoiMessageAlerte(sujet, msg, machine.getNiveauDAlerte())) {//on envoie le msg 
                        machine.setStatue(Bean.START);
                        bean.updateMachie(machine);
                    }
                    //continue;
                } else {//alerte déja envoyer
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le message d'alerte à déja été envoyé pour la machine: <<" + machine.getAdresseIP() + ">>");
                }
            } else if (machine.getStatue().equals(Bean.START) || machine.getStatue().equals(Bean.STOP)) {
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le statue de: <<" + machine.getAdresseIP() + ">> es OK. statue= " + machine.getStatue());
            } else {//le statue es imppossible de joindre l'agent ou machine inaccessible
                //l'alerte n'avait pas encore été envoyer on le fait donc
                sujet = "Alerte machine: <<" + machine.getAdresseIP() + ">> statue = " + machine.getStatue();
                msg = "le statue de: <<" + machine.getAdresseIP() + ">> n'es pas bon!!!. statue= " + machine.getStatue();
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.SEVERE, msg);
                if (bean.envoiMessageAlerte(sujet, msg, machine.getNiveauDAlerte())) {//on envoie le msg d'alerte et on met le statue à alerte
                    machine.setStatue(Bean.ALERTE);
                    bean.updateMachie(machine);
                }

            }

        }
    }

    /**
     * renvoie les alertes des machine et met à jour le statu dans le cas où la
     * machine es de nouveau accessible
     */
    @Schedule(hour = "7-17")//cette tache vas s'exécuté toute les  heure entre 7h et 17h
    //@Schedule(second = "30", minute = "*", hour = "*")
    public void renvoiAlerteMachineEtUpdateMachine() {
        System.out.println("renvoie des alertes machine ou met à jour les alertes machines " + new Date());
        List<Machine> listMachineAlerte = bean.getAllMachineByStatue(Bean.ALERTE);
        for (Machine machine : listMachineAlerte) {
            String msg, sujet,
                    statue = bean.testConnectionMachine(machine);
            if (statue.equals(Bean.START)) {//si le statue de la machine à changé
                sujet = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                msg = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                machine.setStatue(Bean.START);
                bean.updateMachie(machine);
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
            } else {//la machine es toujour inaccessible, on revoie le message d'alerte
                sujet = "Rappel Alerte machine: <<" + machine.getAdresseIP() + ">> statue = " + statue;
                msg = "La machine: <<" + machine.getAdresseIP() + ">> n'es toujours pas accessible: STATUE= " + statue;
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.WARNING, msg);
            }
            bean.envoiMessageAlerte(sujet, msg, machine.getNiveauDAlerte());
        }
    }

    public void initialisationListOS() {
        listOS = new ArrayList<>();
        listOS.add(Bean.OSWINDOWS);
        listOS.add(Bean.OSLinux);
    }

    public void initialisationListTypeStatue() {
        listTypeStatue = new ArrayList<>();
        listTypeStatue.add(Bean.START);
        listTypeStatue.add(Bean.STOP);
    }

    public void initialisationListTypeCompte() {
        listTypeCompte = new ArrayList<>();
        listTypeCompte.add(Bean.TYPE_COMPTE_SUPADMIN);
        listTypeCompte.add(Bean.TYPE_COMPTE_ADMIN);
    }

    public void initialisationListTypeTache() {
        listTypeTache = new ArrayList<>();
        listTypeTache.add(Bean.TACHE_DD);
        listTypeTache.add(Bean.TACHE_PROCESSUS);
        listTypeTache.add(Bean.TACHE_SERVICE);
        listTypeTache.add(Bean.TACHE_PING);
        listTypeTache.add(Bean.TACHE_TELNET);
        listTypeTache.add(Bean.TACHE_DATE_MODIFICATION_DERNIER_FICHIER);
        listTypeTache.add(Bean.TACHE_FICHIER_EXISTE);
        listTypeTache.add(Bean.TACHE_TAILLE_FICHIER);
    }

    @PostConstruct
    public void initialisation() {
        System.out.println("initialisation des variables");
        initialisationListOS();
        initialisationListTypeTache();
        initialisationListTypeCompte();
        initialisationListTypeStatue();

        System.out.println("initialisation de la BD");

        int SEUIL_ALERT_DD = 90;
        String periodecheckPing = " 1 * * * * ?";
        int NB_TENTATIVE_PING = 10;

        String adressTest = "192.168.100.182";
        String periodecheckDD = " */30 * * * * ?";
        String periodecheckProcessus = " 10,40 * * * * ?";
        String periodecheckService = " 15,45 * * * * ?";
        String periodecheckFichierExistant = " 21,51 * * * * ?";
        String periodecheckFichierTaille = " 25,55 * * * * ?";
        String periodecheckTelnet = " 29,59 * * * * ?";
        String periodecheckDateModif = " 35,5 * * * * ?";
        int tailleMaxFichie = 5;
        int tailleMinFichie = -5;
        int seuilDateModif = 360;
        String portEcoute = "9039";

        String resultat = "";

        resultat += "\ninitialisation du serveur :-> " + bean.creerOuModifierServeur("monitoringlmtgroupe@gmail.com", "kefmonitoring", "testali", "OnAEyotL", "Alert LMT", false, true);
        //resultat += "\ncreation de la machine qui sera situe sur le serveur :-> " + creerMachine(ADRESSE_MACHINE_SERVEUR, portEcoute, DEFAUL_PERIODE_CHECK_MACHINE, OSWINDOWS, "machine Serveur");

        resultat += "\ncreation du 1er utilisateur :-> " + bean.creerUtilisateur("kef", "0000", "kemekong", "francois", Bean.TYPE_COMPTE_SUPADMIN, "237699667694", "kemekongfrancois@gmail.com", 1);
        resultat += "\ncreation du 2ième utilisateur :-> " + bean.creerUtilisateur("kemekongfrancois", "0000", "kemekong2", "francois2", Bean.TYPE_COMPTE_SUPADMIN, "237675954517", "kemekongfranois@yahoo.fr", 1);

        resultat += "\ncreation de la machine :-> " + bean.creerMachine(adressTest, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF", 1);

        resultat += "\ncreation de la tache DD :-> " + bean.creerTacheSurveilleDD(adressTest, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adressTest, periodecheckProcessus, "vlc.exe", Bean.START, true, false, "", 1, 3);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adressTest, periodecheckProcessus, "vlc2.exe", Bean.START, true, false, "", 1, 3);
        resultat += "\ncreation de la tache Service :-> " + bean.creerTacheSurveilleService(adressTest, periodecheckService, "Connectify", Bean.START, true, true, true, "", 1);
        resultat += "\ncreation de la tache Ping :-> " + bean.creerTachePing(adressTest, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache Ping 2 :-> " + bean.creerTachePing(adressTest, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier existant :-> " + bean.creerTacheSurveilleFichierExist(adressTest, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier superieur :-> " + bean.creerTacheSurveilleTailleFichier(adressTest, periodecheckFichierTaille, "c:/testMonitoring/Setup_Oscillo.exe", tailleMaxFichie, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier inférieur :-> " + bean.creerTacheSurveilleTailleFichier(adressTest, periodecheckFichierTaille, "c:/testMonitoring/TeamViewer_Setup_fr.exe", tailleMinFichie, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adressTest, periodecheckTelnet, "41.204.94.29,8282", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adressTest, periodecheckDateModif, "C:/testMonitoring/test date modification", seuilDateModif, Bean.START, true, true, "", 1);

        String adressTest2 = "172.16.4.20";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest2, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF virtuel", 1);

        resultat += "\ncreation de la tache DD 2:-> " + bean.creerTacheSurveilleDD(adressTest2, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache processus 2 :-> " + bean.creerTacheSurveilleProcessus(adressTest2, periodecheckProcessus, "vlc.exe", Bean.START, true, false, "", 1, 0);
        resultat += "\ncreation de la tache Service 2 :-> " + bean.creerTacheSurveilleService(adressTest2, periodecheckService, "HUAWEIWiMAX", Bean.START, true, true, true, "", 1);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adressTest2, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adressTest2, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier existant 2 :-> " + bean.creerTacheSurveilleFichierExist(adressTest2, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true, "", 1);

        String adressTest3 = "172.16.4.21";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest3, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "ubuntu", 1);

        resultat += "\ncreation de la tache DD 3:-> " + bean.creerTacheSurveilleDD(adressTest3, periodecheckDD, "/mnt/hgfs", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adressTest3, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adressTest3, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier existant 3 :-> " + bean.creerTacheSurveilleFichierExist(adressTest3, periodecheckFichierExistant, "/home/ubuntu/Desktop/dist/parametre.txt", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier superieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adressTest3, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/log.txt", tailleMaxFichie, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache fichier inférieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adressTest3, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/---keen'v - DIS MOI OUI (MARINA) Clip Officiel - YouTube.flv", tailleMinFichie, Bean.START, true, true, "", 1);
        // resultat += "\ncreation de la tache processus 3 :-> " + bean.creerTacheSurveilleProcessus(adressTest3, periodecheckProcessus, "vlc.exe", START, false);
        //resultat += "\ncreation de la tache Service 3 :-> " + bean.creerTacheSurveilleService(adressTest3, periodecheckService, "HUAWEIWiMAX", START, true, true);

        System.out.println(resultat);

        //création des Machine pour LMT
        resultat = "\n \n initialisation des machines et taches de LMT";
        int i;
        int attenteEtRepetitionProcessus = 4;

        i = 0;
        String adresse = "192.168.100.95";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "ADMIN", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.93";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.81";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "lmt", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.86";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingAlerte_BICEC_New_V2.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingBalance_BIC_New.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingHistory_BIC_New.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "xampp-control.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging_SGC_2016.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging_Sonel_2015_V5.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging83_2015_V5.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkSimpleSMS_Sonel_2015_V5.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "Advans_BulkMessaging_SansTraitementdoublons.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * 3-7", "D:/vas/web/bulk/upload/sgbcmbanking/sgbcfile", seuilDateModif, Bean.START, true, true, "SGBC", 1);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * 2-1", "D:/vas/web/bulk/upload/Tout/BGFI", seuilDateModif, Bean.START, true, true, "BGFI", 1);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * 3-7", "D:/vas/bicec/mBanking-Alerte/info", seuilDateModif, Bean.START, true, true, "BICEC", 1);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * 3-7", "D:/vas/bicec/mBanking-Balance/info", seuilDateModif, Bean.START, true, true, "BICEC", 1);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * 3-7", "D:/vas/bicec/mBanking-History/info", seuilDateModif, Bean.START, true, true, "BICEC", 1);

        i = 0;
        adresse = "192.168.200.150";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "goldsms", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.200.163";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "SICAP", 1);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, (i += 2) + " */10 * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, (i += 2) + " */10 * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.16.38,5016", Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "217.113.69.8", 9000, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "131.166.253.49,7004", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "10.32.251.240,3700", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "196.202.232.250,3700", Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.244.255.6", 5016, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "41.202.220.73,2775", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "41.202.206.65,15019", Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.202.206.69", 15019, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "121.241.242.124,2345", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163,50402", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163,50411", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163,50404", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163,50410", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163,50600", Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 4848, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.150,8282", Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 13001, Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 6013, Bean.START, true, true, "", 1);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 3306, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152,8087", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152,4848", Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152,8080", Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.200.152";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "USSD", 1);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD /tmp -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/tmp", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.77";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.78";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.79";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", 1);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "NCM.EXE", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "onnet64.exe", Bean.START, true, true, "", 1, attenteEtRepetitionProcessus);

        i = 0;
        adresse = "192.168.100.74";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", 1);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        i = 0;
        adresse = "192.168.100.180";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", 1);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", 1);

        System.out.println(resultat);

        //return resultat;
    }

}
