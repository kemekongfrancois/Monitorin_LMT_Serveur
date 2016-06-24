/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import entite.Serveur;
import entite.Tache;
import entite.Utilisateur;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import wsClient.WSClientMonitoring;

/**
 *
 * @author KEF10
 */
@Stateless
public class Bean {

    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux = "Linux";
    public static final String OK = "OK";
    public static final String PB = "PB";
    public static final String KO = "KO";
    public static final String ALERTE = "ALERTE";
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String TACHE_DD = "surveiller_dd";
    public static final String TACHE_PROCESSUS = "surveiller_processus";
    public static final String TACHE_SERVICE = "surveiller_service";
    
    public static final int SEUIL_ALERT_DD = 90;
    public static final String TACHE_SURVEILLER_FICHIER_EXIST = "surveille_fichier_existe";
    public static final String TACHE_SURVEILLE_FICHIER_TAILLE = "surveille_fichier_taille";
    public static final String TACHE_EXISTE_DEJA = "cette tache existe deja sur cette machine";
    public static final String ADRESSE_INCONU = "adresse IP inconue";
    public static final String ECHEC_ECRITURE_BD = "enregistrement dans la BD impossible";
    public static final String ADRESSE_UTILISE = "adresse ip utilise";
    public static final String NUMERO_COUR_INVALIDE = "senderID invalide";
    public static final String INFO_DEJA_EXISTANT_EN_BD = "le login (boite mail ou numero de telephone) es deja utilise";
    
    public static final String DEFAUL_PERIODE_CHECK_MACHINE = "1 1 * * * ?";//represente la valeur par defaut de la période de check des machine 

    //public static final String expresRegulierNumeroTel = "(\\+?237)?\\d{9}";

    @PersistenceContext
    private EntityManager em;
    
    
    
    public String persist(Object object) {
        try {
            em.persist(object);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, object + ": impossible d'écrire cet objet dans la BD", e);
            //Until.savelog("impossible d'écrire dans la BD \n" + e, Until.fichieLog);
            return ECHEC_ECRITURE_BD;
        }
    }

    /**
     * si ladresse ip es déja donnée à une machine on retourne null
     *
     * @param AdresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return
     */
    public String creerMachine(String AdresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachine(AdresIP);
        if (machine != null) {//l'adresse ip es déja utilisé
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, AdresIP + ": cette adresse es déja utilisé");
            return ADRESSE_UTILISE;
        }
        machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setPortEcoute(port);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setTypeOS(nonOS);
        return persist(machine);
    }

    /**
     *
     * @param AdresIP
     * @return null en cas de pb
     */
    public Machine getMachine(String AdresIP) {
        Query query = em.createNamedQuery("Machine.findByAdresseIP");
        query.setParameter("adresseIP", AdresIP);
        if (query.getResultList().isEmpty()) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "machine inexistante");
            return null;
        } else {
            return (Machine) query.getSingleResult();
        }
    }

    private List<Tache> getAllTache() {
        Query requete = em.createNamedQuery("Tache.findAll");
        return (List<Tache>) requete.getResultList();
    }

    /**
     * cette fonction verifi si la machine (donc l'adresse es pris en paramettre) possède
     * une tache avec le nom pris en paramettre
     *
     * @param idMachine
     * @param nomTache
     * @return true si la machine possède ce nom de tache
     */
    private boolean verifiNomTacheSurMachine(String adresIpMachine, String nomTache) {
        List<Tache> listTache = getAllTache();
        for (Tache tache : listTache) {
            if (tache.getNom().equalsIgnoreCase(nomTache) && tache.getIdMachine().getAdresseIP().equals(adresIpMachine)) {
                return true;
            }
        }
        return false;
    }

    /**
     * cette fonction retourne la machine donc les caractéristique sont pris en
     * paramètre si la machine n'existe pas on là créer
     *
     * @param adresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return null en cas de pb
     */
    public Machine verifiOuCreerMachine(String adresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachine(adresIP);
        if (machine == null) {//la machine n'existe pas on la créer
            if (creerMachine(adresIP, port, periodeCheck, nonOS, nomMachine).equals(OK))//on créer l'objet dans la BD
            {
                return getMachine(adresIP);
            } else {
                return null;
            }
        } else {//la machine existe on l'a retourne
            return machine;
        }
    }

    public Tache getTache(int IdMachine) {
        Tache tache = em.find(Tache.class, IdMachine);
        if (tache == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "Tache inexistante");
            return null;
        } else {
            return tache;
        }

    }

    /**
     * cette fonction retourne la liste des adresse email à qui une alerte peut
     * être envoyer
     *
     * @return
     */
    private List<String> getLlisteEmail() {
        List<String> listEmail = new ArrayList<>();
        //listEmail.add("kemekongfranois@yahoo.fr");
        //listEmail.add("kemekongfrancois@gmail.com");
        List<Utilisateur> listUtilisateur = getAllUtilisateur();
        for (Utilisateur utilisateur : listUtilisateur) {
            listEmail.add(utilisateur.getBoiteMail());
        }
        return listEmail;
    }

    private List<Utilisateur> getAllUtilisateur() {
        Query requet = em.createNamedQuery("Utilisateur.findAll");
        return (List<Utilisateur>) requet.getResultList();
    }

    /**
     * cette fonction retourne la liste des numéros aux quelles seront envoyé
     * des alertes
     *
     * @return
     */
    private List<String> getListNumero() {
        List<String> listeNumero = new ArrayList<>();
        //listeNumero.add("237699667694");
        //listeNumero.add("237675954517");
        List<Utilisateur> listUtilisateur = getAllUtilisateur();
        for (Utilisateur utilisateur : listUtilisateur) {
            listeNumero.add(utilisateur.getNumeroTelephone());
        }
        return listeNumero;
    }

    public boolean traitementAlerteTache(int idTache, int codeErreur) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        String corpsEmailEtSMS, sujetEmail;

        switch (tache.getTypeTache()) {
            case TACHE_DD:
                sujetEmail = "Alerte: espace disque sur la machine: " + tache.getIdMachine().getAdresseIP();
                if (codeErreur == 200) {//cas où la lettre de partition ne correspond à aucune partition
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " la lettre de partition ne correspont a aucune partition ou elle es invalide : <<" + tache.getNom() + " >>";
                } else {
                    corpsEmailEtSMS = " espace restant du disque <<" + tache.getNom() + ">>" + "de la machine<<" + tache.getIdMachine().getAdresseIP() + ">> es faible ";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            case TACHE_PROCESSUS:
                sujetEmail = "Alerte: Processus arrete sur: " + tache.getIdMachine().getAdresseIP();
                if (codeErreur == 0) {
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le processus : <<" + tache.getNom() + " >> es arreté";
                } else {//cas où la valeur es 1: il ya un pb
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le processus : <<" + tache.getNom() + " >> n'es pas reconnue";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            case TACHE_SERVICE:
                sujetEmail = "Alerte: Service arrete sur: " + tache.getIdMachine().getAdresseIP();
                if (codeErreur == 0) {
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + ">> es arreté";
                } else {
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + " >> n'es pas reconnue";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            default:
                Logger.getLogger(Bean.class.getName()).log(Level.WARNING, tache.getTypeTache() + ": ce type n'es pas reconnue ");
                return false;
        }
        
        if (envoiMessageAlerte(sujetEmail, corpsEmailEtSMS)) {
            tache.setStatue(ALERTE);
            return true;
        } else {//cas où aucun msg d'alert n'a pus être envoyé
            return false;
        }
    }
    
    /**
     * cette fonction envoie le msg d'alerte au administrateur
     * ces message sont des sms et des mail si un seul des envoie c'est effectué on retourne vrai
     * si l'envoie des mail et SMS à été désactivé on supposera que les msg on été envoiyé (on retournera "tue")
     * @param sujetMail
     * @param corpsEmailEtSMS
     * @return 
     */
    private boolean envoiMessageAlerte(String sujetMail,String corpsEmailEtSMS){
        List<String> listEmail = getLlisteEmail();
        //envoie de l'alerte aux administrateur (si les mail ou les SMS on pus être envoyer alors le problème à été traité)
        Serveur serveur = getServeur();
        if(!serveur.getEnvoialerteSMS() && !serveur.getEnvoieAlerteMail()){
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL es désactivé");
            return true;
        }
        boolean envoiMail=false;
        if(serveur.getEnvoieAlerteMail()) {
            envoiMail = envoieDeMail(listEmail, corpsEmailEtSMS, sujetMail);
        }else{
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes mail es désactivé");
        }
        boolean envoiSMS = false;
        if(serveur.getEnvoialerteSMS()){
            envoiSMS = envoieSMS(corpsEmailEtSMS, getListNumero());
        }else{
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes SMS es désactivé");
        }
        
        if (envoiMail || envoiSMS) {
            return true;
        } else {//cas où aucun msg d'alert n'a pus être envoyé
            return false;
        }
    }

    public boolean activerDesactiveAlertSMS(boolean statut){
        Serveur serveur = getServeur();
        if(serveur==null){
            return false;
        }
        if(statut){
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte SMS ACTIVE");
        }else{
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte SMS DESACTIVE");
        }
        serveur.setEnvoialerteSMS(statut);
        return true;
    }
    
    public boolean activerDesactiveAlertMail(boolean statut){
        Serveur serveur = getServeur();
        if(serveur==null){
            return false;
        }
        if(statut){
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte MAIL ACTIVE");
        }else{
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte MAIL DESACTIVE");
        }
        serveur.setEnvoieAlerteMail(statut);
        return true;
    }
    
    public boolean traitementAlerteMachine(int IdMachine, List<Tache> listTachePB) {
        Machine machine = em.find(Machine.class, IdMachine);
        if (machine == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "machine inexistante: id=" + IdMachine);
            return false;
        }
        if (listTachePB.isEmpty()) {//cas ou il n'ya pas de pb sur la machine
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "communication OK avec la machine: adresse=" + machine.getAdresseIP());
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "alerte machine traiter ");
        }

        return true;
    }

    /**
     * retourne la liste des tache d'une machine donc l'adresse es prise en
     * paramettre
     * @param ipAdresse
     * @return null en cas de pb
     */
    public List<Tache> getListTacheMachine(String ipAdresse) {
        Machine machine = getMachine(ipAdresse);
        if (machine != null) {
            List<Tache> listTacheMachine = new ArrayList<>();
            List<Tache> listTache = getAllTache();
            for (Tache tache : listTache) {
                if(tache.getIdMachine().getIdMachine()==machine.getIdMachine()){
                    listTacheMachine.add(tache);
                }
            }
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches es envoyé à la machine" + ipAdresse + ": nombre de taches=" + listTacheMachine.size());
            return listTacheMachine;
        } else {
            return null;
        }
    }

    public String creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String lettre_partition, int seuil, String statue) {
        if (verifiNomTacheSurMachine(adresIpMachine, lettre_partition)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, lettre_partition + ": cette partition es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_DD, null, periodeVerrification, lettre_partition, seuil, statue, null);
    }
    
    public String creerTacheSurveilleProcessus(String adresIpMachine, String periodeVerrification, String nomProcessus, String statue) {
        if (verifiNomTacheSurMachine(adresIpMachine, nomProcessus)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomProcessus + ": ce processus es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_PROCESSUS, null, periodeVerrification, nomProcessus, 0, statue, null);
    }
    
    public String creerTacheSurveilleService(String adresIpMachine, String periodeVerrification, String nomService, String statue) {
        if (verifiNomTacheSurMachine(adresIpMachine, nomService)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomService + ": ce service es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_SERVICE, null, periodeVerrification, nomService, 0, statue, null);
    }

    private String creerTache(String adresIpMachine, String typeTache, String description_fichier, String periodeVerrification, String nom, int seuil, String statue, String liste_adresse) {
        Machine machine = getMachine(adresIpMachine);
        if (machine == null) {
            return ADRESSE_INCONU;
        }
        
        Tache tache = new Tache();

        tache.setTypeTache(typeTache);
        tache.setIdMachine(machine);
        tache.setSeuilAlerte(seuil);
        tache.setPeriodeVerrification(periodeVerrification);
        tache.setStatue(statue);
        tache.setDescriptionFichier(description_fichier);
        tache.setListeAdresse(liste_adresse);
        tache.setNom(nom);
        return persist(tache);

    }

    /**
     * cette fontion permet d'envoyer un mail à plusieur destinataires
     *
     * @param listAdresseDestinataires
     * @param message
     * @param sujet
     */
    public boolean envoieDeMail(List<String> listAdresseDestinataires, String message, String sujet) {
        try {
            Serveur serveur = getServeur();
            String adresseEmetteur = serveur.getEmailEnvoiMail();
            String passEmetteur = serveur.getPassEnvoiMail();

            Properties props = System.getProperties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.SocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.enable", "false");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            Session mailSession = Session.getDefaultInstance(props, null);
            mailSession.setDebug(false);//on desactive le debugage

            //*******envoi du mail******************
            Message mailMessage = new MimeMessage(mailSession);

            mailMessage.setFrom(new InternetAddress(adresseEmetteur));
            //costruction du tableau d'adresse
            Address[] tabAdresse = new Address[listAdresseDestinataires.size()];
            int i = 0;
            for (String adresse : listAdresseDestinataires) {
                tabAdresse[i++] = new InternetAddress(adresse);
            }

            mailMessage.setRecipients(Message.RecipientType.TO, tabAdresse);
            mailMessage.setContent(message, "text/plain");
            mailMessage.setSubject(sujet);

            Transport transport = mailSession.getTransport("smtp");
            transport.connect("smtp.gmail.com", adresseEmetteur, passEmetteur);

            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
            transport.close();

            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "les mails d'alerte on bien été envoyé");
            return true;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "les mails non pas pus être envoyer \n", e);
            return false;
        }
    }

    /**
     * cette fonction permet d'ouvrire une connection web service vers une
     * machine qu'on supervise
     */
    private WSClientMonitoring appelWSMachineClient(String adresse, String port) {
        try {
            //String adresse = "172.16.4.2";
            //String port = "8088";
            URL url = new URL("http://" + adresse + ":" + port + "/WSClientMonitoring?wsdl");
            wsClient.WSClientMonitoringService service = new wsClient.WSClientMonitoringService(url);
            return service.getWSClientMonitoringPort();
        } catch (Exception ex) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "problème lors de l'appel du web service de la machine: " + adresse + "\n", ex);
            return null;
        }

    }

    public boolean redemarerTache(int idTache) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        String adresse = tache.getIdMachine().getAdresseIP();
        WSClientMonitoring ws = appelWSMachineClient(adresse, tache.getIdMachine().getPortEcoute());
        if (ws == null) {
            return false;
        }
        tache.setStatue(START);
        if (!ws.demarerMetAJourOUStopperTache(tache)) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la machine :" + adresse + " n'a pas pus redémarer la tache");
            return false;
        }
        return true;
    }

    /**
     * cette fonction permet d'initialisé les paraettre de nottre serveur ou de
     * mettre à jour le serveur il n'ya une seule instance de serveur dans la
     * table serveur
     *
     * @param emailEnvoiMail
     * @param passEnvoiMail
     * @param logingSms
     * @param motDePasseSms
     * @return
     */
    public String creerOuModifierServeur(String emailEnvoiMail, String passEnvoiMail, String logingSms, String motDePasseSms, String numeroCour, boolean envoiSMS, boolean envoiMail) {
        Serveur serveur = em.find(Serveur.class, 1);
        if (serveur == null) {
            serveur = new Serveur(1);
        }
        if (numeroCour.length() > 11) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "<" + numeroCour + "> n'est pas valide comme SenderID");
            return NUMERO_COUR_INVALIDE;
        }
        serveur.setEmailEnvoiMail(emailEnvoiMail);
        serveur.setPassEnvoiMail(passEnvoiMail);
        serveur.setLogingSMS(logingSms);
        serveur.setMotdepasseSMS(motDePasseSms);
        serveur.setNumeroCourt(numeroCour);
        serveur.setEnvoialerteSMS(envoiSMS);
        serveur.setEnvoieAlerteMail(envoiMail);
        return persist(serveur);
    }

    public Serveur getServeur() {
        Serveur serveur = em.find(Serveur.class, 1);
        if (serveur == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le serveur n'existe pas, veille initialisé la table serveur");
        }
        return serveur;
    }

    /**
     * cette fontion permet d'envoyer le SMS à une liste de destinataire
     *
     * @param message
     * @param destinataires
     * @return
     */
    private boolean envoieSMS(String message, List<String> destinataires) {
        Serveur serveur = getServeur();
        String userLogoin = serveur.getLogingSMS();
        String userpassword = serveur.getMotdepasseSMS();
        String numeroCourt = serveur.getNumeroCourt();

        String msgAlerte = "";
        try {
            ws_SMS_LMT.ServiceSMS ws = (new ws_SMS_LMT.ServiceSMS_Service()).getServiceSMSPort();
            int resultat = ws.envoyerSMS(userLogoin, userpassword, destinataires, message, numeroCourt);
            switch (resultat) {
                case 1: //le SMS est bien partie
                    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "les SMS ont bien été envoyé");
                    return true;
                case 0:
                    msgAlerte = " Une exception s'est produite dans le systeme lors de l'envoie des SMS";
                    break;
                case 100:
                    msgAlerte = " pas assez de credit SMS ";
                    break;
                case 404:
                    msgAlerte = " login ou mot de passe LMT non valide";
                    break;

                default:
                    msgAlerte = "erreur inconue lors de l'envoie du SMS";
                    break;
            }

            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, msgAlerte);
            envoieDeMail(getLlisteEmail(), msgAlerte, "echec lors de l'envoie des SMS");
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    public String creerUtilisateur(String login, String pass, String nom, String prenom, String type_compte, String numero_telephone, String boite_mail) {
        List<Utilisateur> listUtilisateur = getAllUtilisateur();
        for (Utilisateur utilisateur : listUtilisateur) {//on verifie que les information entré n'existe pas encore dans la BD
            if (login.equalsIgnoreCase(utilisateur.getLogin())
                    || numero_telephone.equalsIgnoreCase(utilisateur.getNumeroTelephone())
                    || boite_mail.equalsIgnoreCase(utilisateur.getBoiteMail())) {
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le login (boite mail ou numero de téléphone) es déja utilisé");
                return INFO_DEJA_EXISTANT_EN_BD;
            }
        }

        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setBoiteMail(boite_mail);
        utilisateur.setLogin(login);
        utilisateur.setNom(nom);
        utilisateur.setNumeroTelephone(numero_telephone);
        utilisateur.setPass(pass);
        utilisateur.setPrenom(prenom);
        utilisateur.setTypeCompte(type_compte);
        return persist(utilisateur);
    }

    public String initialisation() {
        String adressTest = "172.16.4.2";
        String periodecheckDD = " 1,30 * * * * ?";
        String periodecheckProcessus = " 10,40 * * * * ?";
        String periodecheckService= " 15,45 * * * * ?";

        String resultat = "";

        resultat += "\ninitialisation du serveur :-> " + creerOuModifierServeur("jesuisinvisible1@gmail.com", "Kef007007", "testali", "OnAEyotL", "Alert LMT",false,false);
        resultat += "\ncreation de la machine :-> " + creerMachine(adressTest, "8088", DEFAUL_PERIODE_CHECK_MACHINE, "Windows", "KEF");
        resultat += "\ncreation du 1er utilisateur :-> " + creerUtilisateur("kef", "0000", "kemekong", "francois", "supAdmin", "237699667694", "kemekongfrancois@gmail.com");
        resultat += "\ncreation du 2ième utilisateur :-> " + creerUtilisateur("kef2", "0000", "kemekong2", "francois2", "supAdmin", "237675954517", "kemekongfranois@yahoo.fr");
        
        
        resultat += "\ncreation de la tache DD :-> " + creerTacheSurveilleDD(adressTest, periodecheckDD, "c:", SEUIL_ALERT_DD, START);
        resultat += "\ncreation de la tache processus :-> " + creerTacheSurveilleProcessus(adressTest, periodecheckProcessus, "vlc.exe", START);
        resultat += "\ncreation de la tache Service :-> " + creerTacheSurveilleService(adressTest, periodecheckService, "Connectify", START);
        //resultat += "\ndemarer la tache "+redemarerTache(1);
        return resultat;

    }
}
