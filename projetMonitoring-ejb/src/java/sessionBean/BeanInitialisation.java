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
    public static List<String> listTypeStatut;

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
        List<Machine> listMAchineAvecStatut = bean.getAllMachineAvecBonStatut();
        for (Machine machine : listMAchineAvecStatut) {
            Machine machineBD = bean.getMachineByIP(machine.getAdresseIP());//on recupére la machine réel en BD avec le statut
            if (machineBD.getStatut().equals(Bean.ALERTE)) {//on verrifie si on avait déja envoyer cette alerte
                if (machine.getStatut().equals(Bean.START)) {//la machine es de nouveau accessible
                    sujet = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                    msg = sujet;
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
                    if (bean.envoiMessageAlerte(sujet, msg, machine.getNiveauDAlerte())) {//on envoie le msg 
                        machine.setStatut(Bean.START);
                        bean.updateMachie(machine);
                    }
                    //continue;
                } else {//alerte déja envoyer
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le message d'alerte à déja été envoyé pour la machine: <<" + machine.getAdresseIP() + ">>");
                }
            } else if (machine.getStatut().equals(Bean.START) || machine.getStatut().equals(Bean.STOP)) {
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le statut de: <<" + machine.getAdresseIP() + ">> es OK. statut= " + machine.getStatut());
            } else {//le statut es imppossible de joindre l'agent ou machine inaccessible
                //l'alerte n'avait pas encore été envoyer on le fait donc
                sujet = "Alerte machine: <<" + machine.getAdresseIP() + ">> statut = " + machine.getStatut();
                msg = "le statut de: <<" + machine.getAdresseIP() + ">> n'es pas bon!!!. statut= " + machine.getStatut();
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.SEVERE, msg);
                if (bean.envoiMessageAlerte(sujet, msg, machine.getNiveauDAlerte())) {//on envoie le msg d'alerte et on met le statut à alerte
                    machine.setStatut(Bean.ALERTE);
                    bean.updateMachie(machine);
                }

            }

        }
    }

    /**
     * renvoie les alertes des machine et met à jour le statu dans le cas où la
     * machine es de nouveau accessible
     */
    @Schedule(hour = "7,12,16,21", dayOfWeek = "Mon-Sat" )//cette tache vas s'exécuté de lundi à samedi à 7,12,16 et 21 heure
    //@Schedule(second = "30", minute = "*", hour = "*")
    public void renvoiAlerteMachineEtUpdateMachine() {
        System.out.println("renvoie des alertes machine ou met à jour les alertes machines " + new Date());
        List<Machine> listMachineAlerte = bean.getAllMachineByStatut(Bean.ALERTE);
        for (Machine machine : listMachineAlerte) {
            String msg, sujet,
                    statut = bean.testConnectionMachine(machine);
            if (statut.equals(Bean.START)) {//si le statut de la machine à changé
                sujet = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                msg = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible";
                machine.setStatut(Bean.START);
                bean.updateMachie(machine);
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
            } else {//la machine es toujour inaccessible, on revoie le message d'alerte
                sujet = "Rappel Alerte machine: <<" + machine.getAdresseIP() + ">> statut = " + statut;
                msg = "La machine: <<" + machine.getAdresseIP() + ">> n'es toujours pas accessible: STATUE= " + statut;
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

    public void initialisationListTypeStatut() {
        listTypeStatut = new ArrayList<>();
        listTypeStatut.add(Bean.START);
        listTypeStatut.add(Bean.STOP);
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
        listTypeTache.add(Bean.TACHE_UPTIME_MACHINE);
        listTypeTache.add(Bean.TACHE_TEST_LIEN);
    }

    @PostConstruct
    public void initialisation() {
        System.out.println("initialisation des variables");
        initialisationListOS();
        initialisationListTypeTache();
        initialisationListTypeCompte();
        initialisationListTypeStatut();

        System.out.println("initialisation de la BD");

        int SEUIL_ALERT_DD = 90;
        String periodecheckPing = " 1 * * * * ?";
        int NB_TENTATIVE_PING = 5;
        int nbTentativeTelnet = 3;
        int niveauAlerte = Bean.NIVEAU_ALERTE;
        int attenteEtRepetitionProcessus = 4;

        String adresse = "192.168.100.182";
        String periodecheckDD = " */30 * * * * ?";
        String periodecheckProcessus = " 10,40 * * * * ?";
        String periodecheckService = " 15,45 * * * * ?";
        String periodecheckFichierExistant = " 21,51 * * * * ?";
        String periodecheckFichierTaille = " 25,55 * * * * ?";
        String periodecheckTelnet = " 29,59 * * * * ?";
        String periodecheckDateModif = " 35,5 * * * * ?";
        int tailleMaxFichie = 3200;
        int tailleMinFichie = -9409;
        int seuilDateModif = 360;
        String portEcoute = "9039";
        String periodecheckUptime = " 35 * * * * ?";
        int SEUIL_ALERT_UPTIME = 10;
        String periodecheckLien = " 5 * * * * ?";
        int NB_TENTATIVE_LIEN = 5;

        String resultat = "";

        resultat += "\ninitialisation du serveur :-> " + bean.creerOuModifierServeur("monitoringlmtgroupe@gmail.com", "kefmonitoring", "testali", "OnAEyotL", "Alert LMT", false, true);
        //resultat += "\ncreation de la machine qui sera situe sur le serveur :-> " + creerMachine(ADRESSE_MACHINE_SERVEUR, portEcoute, DEFAUL_PERIODE_CHECK_MACHINE, OSWINDOWS, "machine Serveur");

        resultat += "\ncreation du 1er utilisateur :-> " + bean.creerUtilisateur("kef", "0000", "kemekong", "francois", Bean.TYPE_COMPTE_SUPADMIN, "237699667694", "kemekongfrancois@gmail.com", 1);
        resultat += "\ncreation du 2ième utilisateur :-> " + bean.creerUtilisateur("kemekongfrancois", "0000", "kemekong2", "francois2", Bean.TYPE_COMPTE_SUPADMIN, "237675954517", "kemekongfranois@yahoo.fr", niveauAlerte);

        resultat += "\ncreation de la machine :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF", niveauAlerte);

        resultat += "\ncreation de la tache DD :-> " + bean.creerTacheSurveilleDD(adresse, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, periodecheckUptime, SEUIL_ALERT_UPTIME, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, periodecheckProcessus, "vlc.exe", attenteEtRepetitionProcessus, Bean.START, true, false, "", niveauAlerte);
        resultat += "\ncreation de la tache Service :-> " + bean.creerTacheSurveilleService(adresse, periodecheckService, "Connectify", Bean.START, true, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://192.168.100.86:8090/modispatcher", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://41.204.94.27:50402/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://41.204.94.27:50411/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://41.204.94.27:50404/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://41.204.94.27:50600/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheckLien, "http://41.204.94.29:8282/Managesms-war/j_security_check", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier superieur :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheckFichierTaille, "c:/testMonitoring/Setup_Oscillo.exe", tailleMaxFichie, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier inférieur :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheckFichierTaille, "c:/testMonitoring/TeamViewer_Setup_fr.exe", tailleMinFichie, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, periodecheckTelnet, "41.204.94.29:8282", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, periodecheckDateModif, "C:/testMonitoring/test date modification", seuilDateModif, Bean.START, true, true, "", niveauAlerte);

        adresse = "172.16.4.20";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest2, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF virtuel", niveauAlerte);

        resultat += "\ncreation de la tache DD 2:-> " + bean.creerTacheSurveilleDD(adresse, periodecheckDD, "c:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus 2 :-> " + bean.creerTacheSurveilleProcessus(adresse, periodecheckProcessus, "vlc.exe", attenteEtRepetitionProcessus, Bean.START, true, false, "", niveauAlerte);
        resultat += "\ncreation de la tache Service 2 :-> " + bean.creerTacheSurveilleService(adresse, periodecheckService, "HUAWEIWiMAX", Bean.START, true, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant 2 :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheckFichierExistant, "c:/testMonitoring/test.txt", Bean.START, true, true, "", niveauAlerte);

        adresse = "172.16.4.21";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest3, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "ubuntu", niveauAlerte);

        resultat += "\ncreation de la tache DD 3:-> " + bean.creerTacheSurveilleDD(adresse, periodecheckDD, "/mnt/hgfs", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.google.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adresse, periodecheckPing, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant 3 :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheckFichierExistant, "/home/ubuntu/Desktop/dist/parametre.txt", Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier superieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/log.txt", tailleMaxFichie, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier inférieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheckFichierTaille, "/home/ubuntu/Desktop/dist/---keen'v - DIS MOI OUI (MARINA) Clip Officiel - YouTube.flv", tailleMinFichie, Bean.START, true, true, "", niveauAlerte);
        // resultat += "\ncreation de la tache processus 3 :-> " + bean.creerTacheSurveilleProcessus(adressTest3, periodecheckProcessus, "vlc.exe", START, false);
        //resultat += "\ncreation de la tache Service 3 :-> " + bean.creerTacheSurveilleService(adressTest3, periodecheckService, "HUAWEIWiMAX", START, true, true);

        System.out.println(resultat);

        //création des Machine pour LMT
        resultat = "\n \n initialisation des machines, taches et utilisateur de LMT";
        resultat += "\ncreation de l'utilisateur de LMT :-> " + bean.creerUtilisateur("pushsms", "pushsms", "LMT", "monitoring", Bean.TYPE_COMPTE_SUPADMIN, "237699130076", "pushsms@lmtgroup.com", niveauAlerte);

        int i;

        i = 0;
        adresse = "192.168.100.95";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "ADMIN", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.93";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.81";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "lmt", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.86";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "E:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingAlerte_BICEC_New_V2.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingBalance_BIC_New.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "MbankingHistory_BIC_New.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "xampp-control.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging_SGC_2016.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging_Sonel_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkMessaging83_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "BulkSimpleSMS_Sonel_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "Advans_BulkMessaging_SansTraitementdoublons.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", "D:/vas/web/bulk/upload/sgbcmbanking/sgbcfile", seuilDateModif, Bean.START, true, true, "SGBC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 * * ?", "D:/vas/web/bulk/upload/Tout/BGFI", seuilDateModif, Bean.START, true, true, "BGFI", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", "D:/vas/bicec/mBanking-Alerte/info", seuilDateModif, Bean.START, true, true, "BICEC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", "D:/vas/bicec/mBanking-Balance/info", seuilDateModif, Bean.START, true, true, "BICEC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", "D:/vas/bicec/mBanking-History/info", seuilDateModif, Bean.START, true, true, "BICEC", niveauAlerte);

        i = 0;
        adresse = "192.168.200.150";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "goldsms", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.200.163";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "SICAP", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, (i += 2) + " */10 * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, (i += 2) + " */10 * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.16.38:5016", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "217.113.69.8",nbTentativeTelnet , 9000, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "131.166.253.49:7004", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "10.32.251.240:3700", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "196.202.232.250:3700", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.244.255.6",nbTentativeTelnet , 5016, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "41.202.220.73:2775", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "41.202.206.65:15019", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.202.206.69", 15019, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "121.241.242.124:2345", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163:50402", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163:50411", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163:50404", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163:50410", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.163:50600", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 4848, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.150:8282", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 13001, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 6013, Bean.START, true, true, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 3306, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152:8087", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152:4848", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", "192.168.200.152:8080", nbTentativeTelnet, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.200.152";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "USSD", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD /tmp -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/tmp", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.77";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 12 * * ?", SEUIL_ALERT_UPTIME, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.78";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 12 * * ?", SEUIL_ALERT_UPTIME, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.79";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "C:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "D:", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "NCM.EXE", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", "onnet64.exe", attenteEtRepetitionProcessus, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 12 * * ?", SEUIL_ALERT_UPTIME, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.74";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 12 * * ?", SEUIL_ALERT_UPTIME, Bean.START, true, true, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.180";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/home", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", "/", SEUIL_ALERT_DD, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping vers la 76 :-> " + bean.creerTachePing(adresse, (i += 2) + " */10 * * * ?", "192.168.100.76", NB_TENTATIVE_PING, Bean.START, true, true, "Ping vers la 76", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.100.86:8090/modispatcher", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.200.163:50402/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.200.163:50411/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.200.163:50404/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.200.163:50600/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, "0 " + (i += 2) + "/10 * * * ?", "http://192.168.200.150:8282/Managesms-war/j_security_check", NB_TENTATIVE_LIEN, Bean.START, true, true, "", niveauAlerte);

        System.out.println(resultat);

        //return resultat;
    }

}
