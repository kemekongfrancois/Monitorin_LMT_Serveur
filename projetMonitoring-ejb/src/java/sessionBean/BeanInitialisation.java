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
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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

    public static boolean SMS_ENVOYER = false;

    /**
     * verrifie la disponibilité des agents et met à jour le statu dans le cas
     * où la machine est de nouveau accessible
     *
     */
    @Schedule(minute = "*/5", hour = "*")//cette tache vas s'exécuté toute les 5 minute
    //@Schedule(second = "0", minute = "*", hour = "*")
    public void verifieEtUpdateAgent() {
        String sujet, msg;
        System.out.println("verrification du fonctionnement des agents: ");
        List<Machine> listMAchineAvecStatut = bean.getAllMachineAvecBonStatut();
        for (Machine machine : listMAchineAvecStatut) {
            Machine machineBD = bean.getMachineByIP(machine.getAdresseIP());//on recupére la machine réel en BD avec le statut
            if (machineBD.getStatut().equals(Bean.ALERTE)) {//on verrifie si on avait déjà envoyer cette alerte
                if (machine.getStatut().equals(Bean.START)) {//la machine est de nouveau accessible
                    sujet = "La machine : <<" + machine.getAdresseIP() + ">> est de nouveau accessible ";
                    msg = sujet + " (" + new Date() + ")"
                            + "<br/> <a href=\"http://" + Bean.AdresseDuServerEtPort + "/projetMonitoring-war/faces/listTachesMachine.xhtml?adresseMachine=" + machine.getAdresseIP() + "\">Interface de monitoring</a>";
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
                    if (bean.envoiMessageAlerte(sujet, msg, msg, machine.getNiveauDAlerte())) {//on envoie le msg 
                        machine.setStatut(Bean.START);
                        bean.updateMachie(machine);
                    }

                    //continue;
                } else {//alerte déjà envoyer
                    Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le message d'alerte à déjà été envoyé pour la machine: <<" + machine.getAdresseIP() + ">>");
                }
            } else if (machine.getStatut().equals(Bean.START) || machine.getStatut().equals(Bean.STOP)) {
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, "le statut de: <<" + machine.getAdresseIP() + ">> est OK. STATUT= " + machine.getStatut());
            } else {//le statut est imppossible de joindre l'agent ou machine inaccessible
                //l'alerte n'avait pas encore été envoyer on le fait donc
                sujet = "Alerte machine: <<" + machine.getAdresseIP() + ">> STATUT = " + machine.getStatut();
                msg = "le statut de: <<" + machine.getAdresseIP() + ">> n'est pas bon!!!. STATUT= " + machine.getStatut() + " (" + new Date() + ")"
                        + "<br/> <a href=\"http://" + Bean.AdresseDuServerEtPort + "/projetMonitoring-war/faces/listTachesMachine.xhtml?adresseMachine=" + machine.getAdresseIP() + "\">Interface de monitoring</a>";
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.SEVERE, msg);
                if (bean.envoiMessageAlerte(sujet, msg, msg, machine.getNiveauDAlerte())) {//on envoie le msg d'alerte et on met le statut à alerte
                    machine.setStatut(Bean.ALERTE);
                    bean.updateMachie(machine);
                }

            }

        }
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Timeout //cette anotation donne la possibilité à la méthode de s'éxécuter pendant longtemps. sans elle si la methode traine trop le bean va générer un timeOut exception ce qui vas supprimer le bean et donc empécher que la méthode soit exécuter
    @Schedule(minute = "*/13", hour = "*")
    public void verrifieConnection() throws InterruptedException {
        //getAllListNumero();
        //getAllLlisteEmail();
        //on verrifie que la connection internet passe
        System.out.println("verrifie la connection internet");
        String adresse = "8.8.8.8";
        boolean connectioOK = false;
        int i = 0;
        do {
            connectioOK = bean.pinger(adresse, 5);
            Thread.sleep(10 * 1000);
            i++;
            System.out.println(i + "- tentative ping à l'adresse "+adresse);
        } while (i < 4 && !connectioOK);

        if (connectioOK) {//la connection passe
            if (SMS_ENVOYER) {//la connexion internet ne passait pas précédament
                SMS_ENVOYER = false;//on met à jour la variable pour dire que la connection passe
                String msg = "La connexion internet est rétablie (" + new Date() + ")";
                if (bean.envoieSMS(msg, bean.getAllListNumero(), true)) {//le sms a été envoyer
                    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "La connexion internet est rétablie ");
                } else {//le sms d'alerte n'a pas pus être envoyer
                    Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "La connexion internet est rétablie : le sms d’alerte n’a pas pu être envoyer ");
                }
                bean.envoieDeMail(bean.getAllEmail(), msg, msg);
            } else {
                System.out.println("La connexion internet est UP ");
            }
        } else//la connection ne passe pas
        if (SMS_ENVOYER) {//on a déjà envoyer le sms d'alerte
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "La connexion internet ne passe pas : le sms d’alerte a déjà été envoyer ");
        } else {//on n'a pas encore envoyer le msg d'alerte
            SMS_ENVOYER = true;
            if (bean.envoieSMS("La connexion internet ne passe pas (" + new Date() + ")", bean.getAllListNumero(), false)) {//le sms d'alerte à été envoyer
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "La connexion internet ne passe pas");
            } else {//le sms d'alerte n'a pas pus être envoyer
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "La connexion internet ne passe pas : le sms d’alerte n’a pas pu être envoyé ");
            }
        }
    }

    /**
     * renvoie les alertes des machine et met à jour le statu dans le cas où la
     * machine est de nouveau accessible
     */
    @Schedule(hour = "7,12,16,21", dayOfWeek = "Mon-Sat")//cette tache vas s'exécuté de lundi à samedi à 7,12,16 et 21 heure
    //@Schedule(second = "30", minute = "*", hour = "*")
    public void renvoiAlerteMachineEtUpdateMachine() {
        System.out.println("renvoie des alertes machine ou met à jour les alertes machines ");
        List<Machine> listMachineAlerte = bean.getAllMachineByStatut(Bean.ALERTE);
        for (Machine machine : listMachineAlerte) {
            String msg, sujet,
                    statut = bean.testConnectionMachine(machine);
            if (statut.equals(Bean.START)) {//si le statut de la machine à changé
                sujet = "La machine: <<" + machine.getAdresseIP() + ">> est de nouveau accessible ";
                msg = sujet + " (" + new Date() + ")"
                        + " <br/> <a href=\"http://" + Bean.AdresseDuServerEtPort + "/projetMonitoring-war/faces/listTachesMachine.xhtml?adresseMachine=" + machine.getAdresseIP() + "\">Interface de monitoring</a>";
                machine.setStatut(Bean.START);
                bean.updateMachie(machine);
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.INFO, msg);
            } else {//la machine est toujour inaccessible, on revoie le message d'alerte
                sujet = "Rappel Alerte machine: <<" + machine.getAdresseIP() + ">> STATUT = " + statut;
                msg = "La machine: <<" + machine.getAdresseIP() + ">> n'est toujours pas accessible: STATUT= " + statut + " (" + new Date() + ")"
                        + " <br/> <a href=\"http://" + Bean.AdresseDuServerEtPort + "/projetMonitoring-war/faces/listTachesMachine.xhtml?adresseMachine=" + machine.getAdresseIP() + "\">Interface de monitoring</a>";
                Logger.getLogger(BeanInitialisation.class.getName()).log(Level.WARNING, msg);
            }
            bean.envoiMessageAlerte(sujet, msg, sujet, machine.getNiveauDAlerte());
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
        int NB_TENTATIVE_PING = 5;
        int nbTentativeTelnet = 3;
        int niveauAlerte = Bean.NIVEAU_ALERTE;
        int attenteEtRepetitionProcessus = 4;

        String adresse = "192.168.100.182";
        String periodecheck = " */30 * * * * ?";
        String descriptionPeriod = "Toutes les 30 secondes";

        int tailleMaxFichie = 3200;
        int tailleMinFichie = -9409;
        int seuilDateModif = 720;
        String portEcoute = "9039";
        int SEUIL_ALERT_UPTIME = 10;
        int NB_TENTATIVE_LIEN = 5;
        boolean alerteMail = true;
        boolean alerteSMS = false;

        String resultat = "";

        resultat += "\ninitialisation du serveur :-> " + bean.creerOuModifierServeur("monitoringlmtgroupe@gmail.com", "kefmonitoring", "monitoring", "m0nit@r!ng", "Alert LMT", false, true);
        //resultat += "\ncreation de la machine qui sera situe sur le serveur :-> " + creerMachine(ADRESSE_MACHINE_SERVEUR, portEcoute, DEFAUL_PERIODE_CHECK_MACHINE, OSWINDOWS, "machine Serveur");

        resultat += "\ncreation du 1er utilisateur :-> " + bean.creerUtilisateur("kef", "0000", "kemekong", "francois", Bean.TYPE_COMPTE_SUPADMIN, "237699667694", "kemekongfrancois@gmail.com", 1);
        resultat += "\ncreation du 2ième utilisateur :-> " + bean.creerUtilisateur("kemekongfrancois", "0000", "kemekong2", "francois2", Bean.TYPE_COMPTE_SUPADMIN, "237675954517", "kemekongfranois@yahoo.fr", niveauAlerte);

        resultat += "\ncreation de la machine :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF", niveauAlerte);

        resultat += "\ncreation de la tache DD :-> " + bean.creerTacheSurveilleDD(adresse, periodecheck, descriptionPeriod, "c:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "Vérifie la partition C :", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, periodecheck, "Une fois par jour", SEUIL_ALERT_UPTIME, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, periodecheck, descriptionPeriod, "vlc.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Service :-> " + bean.creerTacheSurveilleService(adresse, periodecheck, descriptionPeriod, "Connectify", Bean.START, true, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.google.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://192.168.100.86:8090/modispatcher", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://41.204.94.27:50402/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://41.204.94.27:50411/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://41.204.94.27:50404/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://41.204.94.27:50600/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, periodecheck, descriptionPeriod, "http://41.204.94.29:8282/Managesms-war/j_security_check", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheck, descriptionPeriod, "c:/testMonitoring/test.txt", Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier superieur :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheck, descriptionPeriod, "c:/testMonitoring/Setup_Oscillo.exe", tailleMaxFichie, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier inférieur :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheck, descriptionPeriod, "c:/testMonitoring/TeamViewer_Setup_fr.exe", tailleMinFichie, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, periodecheck, descriptionPeriod, "41.204.94.29:8282", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, periodecheck, descriptionPeriod, "C:/testMonitoring/test date modification", seuilDateModif, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        adresse = "172.16.4.20";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest2, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "KEF virtuel", niveauAlerte);

        resultat += "\ncreation de la tache DD 2:-> " + bean.creerTacheSurveilleDD(adresse, periodecheck, descriptionPeriod, "c:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus 2 :-> " + bean.creerTacheSurveilleProcessus(adresse, periodecheck, descriptionPeriod, "vlc.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Service 2 :-> " + bean.creerTacheSurveilleService(adresse, periodecheck, descriptionPeriod, "HUAWEIWiMAX", Bean.START, true, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.google.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 2 :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant 2 :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheck, descriptionPeriod, "c:/testMonitoring/test.txt", Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        adresse = "172.16.4.21";
        //resultat += "\ncreation de la machine 2 :-> " + bean.creerMachine(adressTest3, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "ubuntu", niveauAlerte);

        resultat += "\ncreation de la tache DD 3:-> " + bean.creerTacheSurveilleDD(adresse, periodecheck, descriptionPeriod, "/mnt/hgfs", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.google.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping 2 3 :-> " + bean.creerTachePing(adresse, periodecheck, descriptionPeriod, "www.yahoo.com", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier existant 3 :-> " + bean.creerTacheSurveilleFichierExist(adresse, periodecheck, descriptionPeriod, "/home/ubuntu/Desktop/dist/parametre.txt", Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier superieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheck, descriptionPeriod, "/home/ubuntu/Desktop/dist/log.txt", tailleMaxFichie, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache fichier inférieur 3 :-> " + bean.creerTacheSurveilleTailleFichier(adresse, periodecheck, descriptionPeriod, "/home/ubuntu/Desktop/dist/---keen'v - DIS MOI OUI (MARINA) Clip Officiel - YouTube.flv", tailleMinFichie, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        // resultat += "\ncreation de la tache processus 3 :-> " + bean.creerTacheSurveilleProcessus(adressTest3, periodecheckProcessus, "vlc.exe", START, false);
        //resultat += "\ncreation de la tache Service 3 :-> " + bean.creerTacheSurveilleService(adressTest3, periodecheckService, "HUAWEIWiMAX", START, true, true);

        System.out.println(resultat);

        String descriptionPeriodeUptime = "une fois par jour";
        String descriptionPeriodeDD = "Une fois par heure";
        String descriptionPeriodeLastDate = "De Mar-Sam : Une fois par jour";
        String descriptionPeriodeProcessus = descriptionPeriodeDD;
        String descriptionPeriodePing = "Toutes les 10 min";
        String descriptionPeriodeLien = descriptionPeriodePing;
        String descriptionPeriodeTelnet = descriptionPeriodePing;
        //création des Machine pour LMT
        resultat = "\n \n initialisation des machines, taches et utilisateur de LMT";
        resultat += "\ncreation de l'utilisateur de LMT :-> " + bean.creerUtilisateur("pushsms", "pushsms", "LMT", "monitoring", Bean.TYPE_COMPTE_SUPADMIN, "237699130076", "pushsms@lmtgroup.com", niveauAlerte);

        int i;

        i = 0;
        adresse = "192.168.100.95";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "ADMIN", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "E:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.93";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "D:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "E:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.81";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "lmt", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache Ping vers la 76 :-> " + bean.creerTachePing(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodePing, "192.168.100.76", NB_TENTATIVE_PING, Bean.START, alerteMail, alerteSMS, "Ping vers la 76", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.100.86:8090/modispatcher", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.200.163:50402/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.200.163:50411/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.200.163:50404/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.200.163:50600/backoffice.view", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache test de lien :-> " + bean.creerTacheTestLien(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeLien, "http://192.168.200.150:8282/Managesms-war/j_security_check", NB_TENTATIVE_LIEN, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.86";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "D:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD E:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "E:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "MbankingAlerte_BICEC_New_V2.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "MbankingBalance_BIC_New.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "MbankingHistory_BIC_New.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "xampp-control.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "BulkMessaging_SGC_2016.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "BulkMessaging_Sonel_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "BulkMessaging83_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "BulkSimpleSMS_Sonel_2015_V5.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "Advans_BulkMessaging_SansTraitementdoublons.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", descriptionPeriodeLastDate, "D:/vas/web/bulk/upload/sgbcmbanking/sgbcfile", seuilDateModif, Bean.START, alerteMail, alerteSMS, "SGBC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * MON-SAT", "De Lun-Sam : Une fois par jour", "D:/vas/web/bulk/upload/Tout/BGFI", seuilDateModif, Bean.START, alerteMail, alerteSMS, "BGFI", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", descriptionPeriodeLastDate, "D:/vas/bicec/mBanking-Alerte/info", seuilDateModif, Bean.START, alerteMail, alerteSMS, "BICEC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", descriptionPeriodeLastDate, "D:/vas/bicec/mBanking-Balance/info", seuilDateModif, Bean.START, alerteMail, alerteSMS, "BICEC", niveauAlerte);
        resultat += "\ncreation de la tache verrifie date modification dernier fichier :-> " + bean.creerTacheDateModificationDernierFichier(adresse, "0 " + (i += 2) + " 12 ? * TUE-SAT", descriptionPeriodeLastDate, "D:/vas/bicec/mBanking-History/info", seuilDateModif, Bean.START, alerteMail, alerteSMS, "BICEC", niveauAlerte);

        i = 0;
        adresse = "192.168.200.150";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "goldsms", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.200.163";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "SICAP", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/home", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.16.38:5016", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "217.113.69.8",nbTentativeTelnet , 9000, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "131.166.253.49:7004", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "10.32.251.240:3700", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "196.202.232.250:3700", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.244.255.6",nbTentativeTelnet , 5016, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "41.202.220.73:2775", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "41.202.206.65:15019", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "41.202.206.69", 15019, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "121.241.242.124:2345", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.163:50402", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.163:50411", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.163:50404", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.163:50410", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.163:50600", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 4848, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.150:8282", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 13001, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 6013, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        //resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, "0 "+(i+=2)+" * * * ?", "192.168.200.150", 3306, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.152:8087", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.152:4848", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache faire telnet :-> " + bean.creerTacheTelnet(adresse, (i += 2) + " */10 * * * ?", descriptionPeriodeTelnet, "192.168.200.152:8080", nbTentativeTelnet, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.200.152";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "USSD", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/home", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD /tmp -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/tmp", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.77";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "D:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 8 * * ?", descriptionPeriodeUptime, SEUIL_ALERT_UPTIME, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.78";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "D:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 8 * * ?", descriptionPeriodeUptime, SEUIL_ALERT_UPTIME, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.79";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSWINDOWS, "Administrateur", niveauAlerte);
        resultat += "\ncreation de la tache DD C:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "C:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD D:-> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "D:", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "NCM.EXE", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache processus :-> " + bean.creerTacheSurveilleProcessus(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeProcessus, "onnet64.exe", attenteEtRepetitionProcessus, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 8 * * ?", descriptionPeriodeUptime, SEUIL_ALERT_UPTIME, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.74";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/home", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache redémarrage :-> " + bean.creerTacheUptimeMachine(adresse, "0 " + (i += 2) + " 8 * * ?", descriptionPeriodeUptime, SEUIL_ALERT_UPTIME, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        i = 0;
        adresse = "192.168.100.180";
        resultat += "\ncreation de la machine " + adresse + " :-> " + bean.creerMachine(adresse, portEcoute, Bean.DEFAUL_PERIODE_CHECK_MACHINE, Bean.DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE, Bean.OSLinux, "root", niveauAlerte);
        resultat += "\ncreation de la tache DD /home -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/home", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);
        resultat += "\ncreation de la tache DD / -> " + bean.creerTacheSurveilleDD(adresse, "0 " + (i += 2) + " * * * ?", descriptionPeriodeDD, "/", SEUIL_ALERT_DD, Bean.START, alerteMail, alerteSMS, "", niveauAlerte);

        System.out.println(resultat);

        //return resultat;
    }

}
