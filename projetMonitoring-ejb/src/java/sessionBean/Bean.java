/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import com.sun.mail.smtp.SMTPTransport;
import entite.Machine;
import entite.Serveur;
import entite.Tache;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import until.Until;
import wsClient.WSClientMonitoring;

/**
 *
 * @author KEF10
 */
@Stateless
public class Bean {

    public static final String OSWINDOWS = "Windows";
    public static final String OSLinux = "Linux";
    public static final String OK = "ok";
    public static final String DEFAUL_PERIODE_CHECK_MACHINE = "1 * * * * ?";//represente la valeur par defaut de la période de check des machine 
    public static final String ALERTE = "ALERTE";
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final int SEUIL_ALERT_DD = 90;
    public static final String TACHE_DD = "surveiller_dd";
    public static final String TACHE_SURVEILLER_FICHIER_EXIST = "surveille_fichier_existe";
    public static final String SURVEILLE_FICHIER_TAILLE = "surveille_fichier_taille";

    @PersistenceContext(unitName = "projetMonitoring-ejbPU")
    private EntityManager em;

    public boolean persist(Object object) {
        try {
            em.persist(object);
            return true;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, object + ": impossible d'écrire cet objet dans la BD", e);
            //Until.savelog("impossible d'écrire dans la BD \n" + e, Until.fichieLog);
            return false;
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
    public Machine creerMachine(String AdresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachine(AdresIP);
        if (machine != null) {//l'adresse ip es déja utilisé
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, AdresIP + ": cette adresse es déja utilisé");
            return null;
        }
        machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setPortEcoute(port);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setTypeOS(nonOS);
        if(persist(machine)){//on creer la machine dans la BD
            return machine;
        }
        return null;
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
            return creerMachine(adresIP, port, periodeCheck, nonOS, nomMachine);//on créer l'objet dans la BD
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
        listEmail.add("kemekongfranois@yahoo.fr");
        listEmail.add("kemekongfrancois@gmail.com");
        return listEmail;
    }

    public boolean traitementAlerteTache(int idTache, int codeErreur) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        String corpsEmail, sujetEmail;
        List<String> listEmail = getLlisteEmail();

        switch (tache.getTypeTache()) {
            case TACHE_DD:
                sujetEmail = "Alerte: espace disque sur la machine: " + tache.getIdMachine().getAdresseIP();
                if (codeErreur == 200) {//cas où la lettre de partition ne correspond à aucune partition
                    corpsEmail = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " la lettre de partition ne correspont a aucune partition ou elle es invalide : <<" + tache.getNom() + " >>";
                } else {
                    corpsEmail = " espace restant du disque <<" + tache.getNom() + ">>" + "de la machine<<" + tache.getIdMachine().getAdresseIP() + ">> es faible ";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            default:
                Logger.getLogger(Bean.class.getName()).log(Level.WARNING, tache.getTypeTache() + ": ce type n'es pas reconnue ");
                return false;
        }
        //envoie de l'alerte aux administrateur
        if (!envoieDeMail(listEmail, corpsEmail, sujetEmail)) {
            return false;
        }

        tache.setStatue(ALERTE);
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
     *
     * @param ipAdresse
     * @return null en cas de pb
     */
    public List<Tache> getListTacheMachine(String ipAdresse) {
        Machine machine = getMachine(ipAdresse);
        if (machine != null) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches es envoyé à la machine" + ipAdresse + ": nombre de taches=" + machine.getTacheList().size());
            return machine.getTacheList();
        } else {
            return null;
        }
    }

    public Tache creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String lettre_partition, int seuil, String statue) {
        Machine machine = getMachine(adresIpMachine);
        if (machine == null) {
            return null;
        }
        for (Tache tacheVerification : machine.getTacheList()) {
            if (tacheVerification.getNom().equalsIgnoreCase(lettre_partition)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, lettre_partition + ": cette partition es déja surveillé sur la machine: " + adresIpMachine);
                return null;
            }
        }
        return creerTache(machine, TACHE_DD, null, periodeVerrification, lettre_partition, seuil, statue, null);
    }

    private Tache creerTache(Machine machine, String typeTache, String description_fichier, String periodeVerrification, String nom, int seuil, String statue, String liste_adresse) {
        Tache tache = new Tache();

        tache.setTypeTache(typeTache);
        tache.setIdMachine(machine);
        tache.setSeuilAlerte(seuil);
        tache.setPeriodeVerrification(periodeVerrification);
        tache.setStatue(statue);
        tache.setDescriptionFichier(description_fichier);
        tache.setListeAdresse(liste_adresse);
        tache.setNom(nom);
        if (persist(tache)) {//on enregistre la tache dans la BD
            return tache;
        }
        return null;

    }

    /**
     * cette fontion permet d'envoi un mail à plusieur destinataires
     *
     * @param adresseEmetteur
     * @param passEmetteur
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
            mailSession.setDebug(true);

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
    public Serveur creerOuModifierServeur(String emailEnvoiMail, String passEnvoiMail, String logingSms, String motDePasseSms) {
        Serveur serveur = em.find(Serveur.class, 1);
        if (serveur == null) {
            serveur = new Serveur(1);
        }
        serveur.setEmailEnvoiMail(emailEnvoiMail);
        serveur.setPassEnvoiMail(passEnvoiMail);
        serveur.setLogingSMS(logingSms);
        serveur.setMotdepasseSMS(motDePasseSms);
        if (persist(serveur)) {
            return serveur;
        }else{
            return null;
        }
    }

    public Serveur getServeur() {
        Serveur serveur = em.find(Serveur.class, 1);
        if (serveur == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le serveur n'existe pas");
        }
        return serveur;
    }
    /*
    private boolean envoieSMS(String sms, ){
        ServiceSMS ws = (new ServiceSMS_Service()).getServiceSMSPort();
                                int resultat = ws.envoyerSMS(userLogoin, userpassword, destinataires, message, numeroCourt);
    }
     */
}
