/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBean;

import entite.Machine;
import entite.Tache;
import entite.Serveur;
import entite.Utilisateur;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
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
    public static final String TACHE_DD = "Disque";
    public static final String TACHE_PROCESSUS = "Processus";
    public static final String TACHE_SERVICE = "Service";
    public static final String TACHE_PING = "Ping";
    public static final String TACHE_TELNET = "Telnet";
    public static final String TACHE_DATE_MODIFICATION_DERNIER_FICHIER = "Last Date";
    public static final String TACHE_FICHIER_EXISTE = "Fichier existe";
    public static final String TACHE_TAILLE_FICHIER = "Taille fichier";

    public static final String PB_AGENT = "Impossible de contacter l’agent";
    public static final String INACCESSIBLE = "Inaccessible";

    //public static final String ADRESSE_MACHINE_SERVEUR = "127.0.0.1";//cette adresse represent l'adresse du serveur
    public static final String DEFAUL_PERIODE_CHECK_MACHINE = "0 7-22 * * * ?";//represente la valeur par defaut de la période de check des machines (toute les heures entre 7h et 22h)
    public static final int NB_TENTATIVE_PING_LOCAL = 2;
    public static final String TACHE_EXISTE_DEJA = "cette tache existe deja sur cette machine";
    public static final String TACHE_INEXISTANTE = "cette tache n'existe pas";
    public static final String ADRESSE_INCONU = "adresse IP inconue";
    public static final String ECHEC_ECRITURE_BD = "enregistrement dans la BD impossible";
    public static final String ADRESSE_UTILISE = "adresse ip utilise";
    public static final String NUMERO_COUR_INVALIDE = "senderID invalide";
    public static final String INFO_DEJA_EXISTANT_EN_BD = "le login ou la boite mail ou le numero de téléphone es déja utilisé";

    public String OS_MACHINE;
    //public static final String expresRegulierNumeroTel = "(\\+?237)?\\d{9}";
    @PersistenceContext
    private EntityManager em;

    /**
     * cette fonction permet de définir l'os dans le que se trouve notre ejb et
     * de stoque la valeur dans la variable "OS_MACHINE"
     */
    @PostConstruct
    private void miseAjourTypeOS() {
        //---------on recupere le type d'OS du système------
        OS_MACHINE = System.getProperty("os.name");
        if (OS_MACHINE.contains(OSWINDOWS)) {
            OS_MACHINE = OSWINDOWS;
        } else {
            OS_MACHINE = OSLinux;
        }
        //System.out.println("OS= "+ OS_MACHINE);
    }

    /**
     *
     * @param object
     * @return OK,ECHEC_ECRITURE_BD
     */
    private String persist(Object object) {
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
     * @param AdresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return null si ladresse ip es déja donnée à une machine
     */
    public String creerMachine(String AdresIP, String port, String periodeCheck, String nonOS, String nomMachine, int niveauDAlerte) {
        Machine machine = getMachineByIP(AdresIP);
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
        machine.setNiveauDAlerte(niveauDAlerte);
        machine.setStatue(STOP);
        return persist(machine);
    }

    /**
     * cette fonction retourne la machine donc les caractéristique sont pris en
     * paramètre si la machine n'existe pas on là créer. Elle permet aussi de
     * mettre à jour la tache serveur qui vas permettre de faire un ping vers la
     * machine qui viend d'être créer
     *
     * @param adresIP
     * @param port
     * @param periodeCheck
     * @param nonOS
     * @param nomMachine
     * @return null en cas de pb
     */
    public Machine creerOuVerifiMachine(String adresIP, String port, String periodeCheck, String nonOS, String nomMachine) {
        Machine machine = getMachineByIP(adresIP);
        if (machine == null) {//la machine n'existe pas on la créer
            if (creerMachine(adresIP, port, periodeCheck, nonOS, nomMachine, 1).equals(OK))//on créer l'objet dans la BD
            {
                //creerTachePing(ADRESSE_MACHINE_SERVEUR, periodecheckPing, adresIP, NB_TENTATIVE_PING, STOP, true, true);
                return getMachineByIP(adresIP);
            } else {
                return null;
            }
        } else {//la machine existe on l'a retourne
            return machine;
        }
    }

    /**
     *
     * @param AdresIP
     * @return null en cas de pb
     */
    public Machine getMachineByIP(String AdresIP) {
        Query query = em.createNamedQuery("Machine.findByAdresseIP");
        query.setParameter("adresseIP", AdresIP);
        if (query.getResultList().isEmpty()) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "machine inexistante");
            return null;
        } else {
            Machine machine = (Machine) query.getSingleResult();
            //em.refresh(machine);//cette instruction va permettre que la liste des taches de la machine soit charger
            return machine;
        }
    }

    /**
     * est à suprimer cette fonction permet de changer le statue d'une machine
     *
     * @param id
     * @param statue si elle vaut "true" alors le statue sera START sinon elle
     * sera STOP
     * @return
     */
    /* public String changerStatueMachine(Integer id, boolean statue) {
    Machine machine = em.find(Machine.class, id);
    if (statue) {
    machine.setStatue(START);
    } else {
    machine.setStatue(STOP);
    }
    
    return persist(machine);
    }*/
    /**
     * permet d'enregistrer les modification aporté à une machine
     *
     * @param machine
     * @return ADRESSE_UTILISE,OK et l'exception
     */
    public String updateMachie(Machine machine) {
        try {
            String adressIpMachineCourante = machine.getAdresseIP();
            Machine machinePrecedente = getMachineByIP(adressIpMachineCourante);
            if (machinePrecedente != null) {
                if (machinePrecedente.getIdMachine() != machine.getIdMachine()) {//l'adresse ip es déja utilisé sur une autre machine
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, adressIpMachineCourante + ": cette adresse es déja utilisé");
                    return ADRESSE_UTILISE;
                }
            }
            em.merge(machine);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            return e + "";
        }
    }

    public boolean suprimeerMachine(String adresseIP) {
        Machine machine = getMachineByIP(adresseIP);
        if (machine == null) {
            return false;
        }
        try {
            em.remove(machine);
            return true;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible de suprimer la machine " + machine.getAdresseIP(), e);
            return false;
        }
    }

    public List<Tache> getAllTache() {
        Query requete = em.createNamedQuery("Tache.findAll", Tache.class);
        return requete.getResultList();
    }

    private List<Machine> getAllMachine() {
        Query requete = em.createNamedQuery("Machine.findAll", Machine.class);
        return requete.getResultList();
    }

    /**
     * retournne toute les machines donc le statue es passé en paramettre
     *
     * @param statue
     * @return
     */
    public List<Machine> getAllMachineByStatue(String statue) {
        Query query = em.createNamedQuery("Machine.findByStatue", Machine.class);
        query.setParameter("statue", statue);
        return query.getResultList();
    }

    /**
     * cette fonction verifi si la machine (donc l'adresse es pris en
     * paramettre) possède une tache avec le nom pris en paramettre
     *
     * @param adresIpMachine
     * @param nomTache
     * @return true si la machine possède ce nom de tache
     */
    public boolean verifiNomTacheSurMachine(String adresIpMachine, String nomTache) {

        Query query = em.createQuery("SELECT t FROM Tache t WHERE t.idMachine.adresseIP = :adresseIP AND t.nom = :nom", Tache.class);
        query.setParameter("adresseIP", adresIpMachine);
        query.setParameter("nom", nomTache);
        return !query.getResultList().isEmpty();

        /*List<Tache> listTache = getListTacheMachine(adresIpMachine);
        if (listTache == null) {
        return false;
        }
        for (Tache tache : listTache) {
        if (tache.getNom().equalsIgnoreCase(nomTache)) {
        return true;
        }
        }
        return false;*/
    }

    public Tache getTache(int IdTache) {
        Tache tache = em.find(Tache.class, IdTache);
        if (tache == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "Tache inexistante");
            return null;
        } else {
            return tache;
        }

    }

    /**
     * permet d'enregistrer les modification aporté à une tache
     *
     * @param tache
     * @return TACHE_EXISTE_DEJA,OK, l'exception
     */
    public String updateTache(Tache tache) {
        try {
            Tache tachePrecedente = getTache(tache.getIdTache());
            if (!tachePrecedente.getNom().equals(tache.getNom())) {//cas où on veux changé le nom de la tache
                if (verifiNomTacheSurMachine(tache.getIdMachine().getAdresseIP(), tache.getNom())) {
                    return TACHE_EXISTE_DEJA;
                }
            }
            em.merge(tache);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            return e + "";
        }
    }

    public boolean supprimerTache(int idTache) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }

        try {
            //em.merge(tache);
            em.remove(tache);
            return true;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible de suprimer la tache " + tache.getIdTache(), e);
            return false;
        }
    }

    /**
     * cette fonction retourne la liste des adresse mail des utilisateur donc
     * leur niveau d'alerte es inférieur ou égal à la valeur pris en paramettre
     *
     * @param niveauDAlerte
     * @return
     */
    private List<String> getLlisteEmail(int niveauDAlerte) {
        List<String> listEmail = new ArrayList<>();
        //listEmail.add("kemekongfranois@yahoo.fr");
        //listEmail.add("kemekongfrancois@gmail.com");
        Query query = em.createQuery("SELECT u.boiteMail FROM Utilisateur u WHERE u.niveauDAlerte <= :niveauDAlerte", Utilisateur.class);
        query.setParameter("niveauDAlerte", niveauDAlerte);
        listEmail = query.getResultList();

        /*List<Utilisateur> listUtilisateur = getAllUtilisateur();
        for (Utilisateur utilisateur : listUtilisateur) {
        listEmail.add(utilisateur.getBoiteMail());
        }*/
        return listEmail;
    }

    public List<Utilisateur> getAllUtilisateur() {
        Query requet = em.createNamedQuery("Utilisateur.findAll", Utilisateur.class);
        return requet.getResultList();
    }

    public Utilisateur getUtilisateur(Integer idUtilisateur) {
        Utilisateur utilisateur = em.find(Utilisateur.class, idUtilisateur);
        if (utilisateur == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "Utilisateur inexistante");
            return null;

        }
        return utilisateur;
    }

    public String updateUtilisateur(Utilisateur utilisateur) {
        try {
            em.merge(utilisateur);
            return OK;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            return PB;
        }
    }

    public boolean supprimerUtilisateur(int idUtilisateur) {
        Utilisateur utilisateur = getUtilisateur(idUtilisateur);
        if (utilisateur == null) {
            return false;
        }
        try {
            //em.merge(tache);
            em.remove(utilisateur);
            return true;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible de suprimer l'utilisateur " + utilisateur.getIdUtilisateur(), e);
            return false;
        }
    }

    public Utilisateur getUtilisateurByloginAndPass(String login, String pass) {
        Query query = em.createQuery("SELECT u FROM Utilisateur u WHERE u.login = :login AND u.pass = :pass", Utilisateur.class);
        query.setParameter("login", login);
        query.setParameter("pass", pass);

        List<Utilisateur> listUtilisateur = query.getResultList();
        if (listUtilisateur.size() < 1) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le login :" + login + " et le pass: " + pass + " ne coresponde à aucun utilisateur");
            return null;
        } else {
            return listUtilisateur.get(0);
        }
    }

    /**
     * cette fonction retourne la liste des numéros des utilisateur donc leur
     * niveau d'alerte es inférieur ou égal à la valeur pris en paramettre
     *
     * @param niveauDAlerte
     * @return
     */
    private List<String> getListNumero(int niveauDAlerte) {
        List<String> listeNumero = new ArrayList<>();
        //listeNumero.add("237699667694");
        //listeNumero.add("237675954517");
        Query query = em.createQuery("SELECT u.numeroTelephone FROM Utilisateur u WHERE u.niveauDAlerte <= :niveauDAlerte", Utilisateur.class);
        query.setParameter("niveauDAlerte", niveauDAlerte);
        listeNumero = query.getResultList();
        /*List<Utilisateur> listUtilisateur = getAllUtilisateur();
            for (Utilisateur utilisateur : listUtilisateur) {
            listeNumero.add(utilisateur.getNumeroTelephone());
            }*/
        return listeNumero;
    }

    public boolean traitementAlerteTache(int idTache, int codeErreur) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            //Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la tache <<" + idTache + ">> n'existe pas dans la BD");
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
                } else if (codeErreur == -1) {
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + " >> a été redémarer par l'agent";
                } else {
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + " >> n'es pas reconnue";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            case TACHE_PING:
                sujetEmail = "Alerte: impossible de contacter: " + tache.getNom();
                corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le ping vers : <<" + tache.getNom() + ">> ne passe pas";

                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            case TACHE_FICHIER_EXISTE:
                sujetEmail = "Alerte: le fichier <<" + tache.getNom() + ">> n'existe pas";
                corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> n'existe pas";

                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            case TACHE_TAILLE_FICHIER:
                if (codeErreur == -1) {
                    sujetEmail = "Alerte: le fichier <<" + tache.getNom() + ">> n'es pas valide ou il y'a un problème inconue";
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> n'es pas valide ou il y'a un problème inconue";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                } else if (tache.getSeuilAlerte() < 0) {//cas où on verrifie que le fichier à surveille es toujour plus grand que le seuil
                    sujetEmail = "Alerte: le fichier <<" + tache.getNom() + ">> es inférieure à la taille autorisé";
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> es inférieure à la taille autorisé: seuil=" + tache.getSeuilAlerte();
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                } else {//cas où on verrifie que le fichier à surveille es toujour plus petit que le seuil
                    sujetEmail = "Alerte: le fichier <<" + tache.getNom() + ">> es supérieure à la taille autorisé";
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> es supérieure à la taille autorisé: seuil=" + tache.getSeuilAlerte();
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                }

                break;
            case TACHE_DATE_MODIFICATION_DERNIER_FICHIER:
                if (codeErreur == -1) {
                    sujetEmail = "Alerte: le repertoir <<" + tache.getNom() + ">> n'es pas valide ou il y'a un problème inconue";
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le repertoir : <<" + tache.getNom() + ">> n'es pas valide ou il y'a un problème inconue";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                } else {
                    sujetEmail = "Alerte: la date de modification du dernier fichier contenue dans le repertoire <<" + tache.getNom() + ">> n'es pas valide";
                    corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " la date de modification du dernier fichier contenue dans le repertoire <<" + tache.getNom() + ">> n'es pas valide";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                }

                break;
            case TACHE_TELNET:
                sujetEmail = "Alerte: impossible de contacter: " + tache.getNom();
                corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le telnet vers : <<" + tache.getNom() + ">> ne passe pas";

                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmailEtSMS);
                break;
            default:
                Logger.getLogger(Bean.class.getName()).log(Level.WARNING, tache.getTypeTache() + ": ce type n'es pas reconnue ");
                return false;
        }

        //*************envoie des messages d'alerte*********************
        envoiMessageAlertePourTache(tache, corpsEmailEtSMS, sujetEmail);
        tache.setStatue(ALERTE);
        return true;

    }

    /**
     * cette fonction es invoque lorsque une tache qui avais un pb ne l'a plus,
     * elle permet d'informer les administrateur qu'un problème précédament
     * signalé es de nouveau normal
     *
     * @param idTache
     * @return
     */
    public boolean problemeTacheResolu(int idTache) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        String corpsEmailEtSMS = "La situation es de nouveau normal pour la tache:  nom= <<" + tache.getNom() + ">>, type= <<" + tache.getTypeTache() + ">> AdresseIP= <<" + tache.getIdMachine().getAdresseIP() + ">>";
        String sujetEmail = "Situation OK pour la tache << id=" + tache.getIdTache() + " adresse machine = " + tache.getIdMachine().getAdresseIP() + ">>";
        Logger.getLogger(Bean.class.getName()).log(Level.INFO, corpsEmailEtSMS);
        envoiMessageAlertePourTache(tache, corpsEmailEtSMS, sujetEmail);
        tache.setStatue(START);
        return true;

    }

    /**
     * cette fonction envoie le msg d'alerte au administrateur ces message sont
     * des sms et des mail si un seul des envoie c'est effectué on retourne vrai
     * si l'envoie des mail et SMS à été désactivé on supposera que les msg on
     * été envoiyé (on retournera "true"). cette fonction envoie l'alerte
     * suivant les caractéristique de la tache pris en paramettre
     *
     * @param tache
     * @param corpsEmailEtSMS
     * @param sujetEmail
     * @return
     */
    private boolean envoiMessageAlertePourTache(Tache tache, String corpsEmailEtSMS, String sujetEmail) {
        Serveur serveur = getServeurOuInitialiseBD();
        if (!serveur.getEnvoialerteSMS() && !serveur.getEnvoieAlerteMail()) {//cas où les msg d'alerte sont désactivé au niveau serveur (à ne pas suprimer)
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL es désactivé au niveau serveur");
            //tache.setStatue(ALERTE);
            return true;
        }
        if (!tache.getEnvoiyerAlerteMail() && !tache.getEnvoyerAlerteSms()) {//cas où les msg d'alerte sont désactivé au niveau de la tache (à ne pas suprimer)
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL es désactivé au niveau de la tache");
            //tache.setStatue(ALERTE);
            return true;
        }

        boolean traiter = false;
        if (serveur.getEnvoieAlerteMail()) {
            if (tache.getEnvoiyerAlerteMail()) {
                if (envoieDeMail(getLlisteEmail(tache.getNiveauDAlerte()), corpsEmailEtSMS, sujetEmail)) {
                    traiter = true;
                }
            } else {
                Logger.getLogger(Bean.class.getName()).log(Level.INFO, "ALerte mail désactivé sur la tache<<" + tache.getIdTache() + ">>");
            }
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "ALerte mail désactivé sur le serveur");
        }

        if (serveur.getEnvoialerteSMS()) {
            if (tache.getEnvoyerAlerteSms()) {
                if (envoieSMS(corpsEmailEtSMS, getListNumero(tache.getNiveauDAlerte()))) {
                    traiter = true;
                }
            } else {
                Logger.getLogger(Bean.class.getName()).log(Level.INFO, "ALerte SMS désactivé sur la tache<<" + tache.getIdTache() + ">>");
            }
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "ALerte SMS désactivé sur le serveur");
        }

        if (traiter) {//cas où au moins un msg d'alerte a été envoyé
            return true;
        }
        Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "Aucun message d'alerte n'a pus être envoyé");
        return false;
    }

    /**
     * cette fonction envoie le msg d'alerte au administrateur ces message sont
     * des sms et des mail si un seul des envoie c'est effectué on retourne vrai
     * si l'envoie des mail et SMS à été désactivé on supposera que les msg on
     * été envoiyé (on retournera "true")
     *
     * @param sujetMail
     * @param corpsEmailEtSMS
     * @param niveauDAlert
     * @return
     */
    public boolean envoiMessageAlerte(String sujetMail, String corpsEmailEtSMS, int niveauDAlert) {
        //envoie de l'alerte aux administrateur (si les mail ou les SMS on pus être envoyer alors le problème à été traité)
        Serveur serveur = getServeurOuInitialiseBD();
        if (!serveur.getEnvoialerteSMS() && !serveur.getEnvoieAlerteMail()) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL es désactivé");
            return true;
        }
        boolean envoiMail = false;
        if (serveur.getEnvoieAlerteMail()) {
            envoiMail = envoieDeMail(getLlisteEmail(niveauDAlert), corpsEmailEtSMS, sujetMail);
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes mail es désactivé");
        }
        boolean envoiSMS = false;
        if (serveur.getEnvoialerteSMS()) {
            envoiSMS = envoieSMS(corpsEmailEtSMS, getListNumero(niveauDAlert));
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes SMS es désactivé");
        }

        if (envoiMail || envoiSMS) {
            return true;
        } else {//cas où aucun msg d'alert n'a pus être envoyé
            return false;
        }
    }

    /*
    public boolean activerDesactiveAlertSMS(boolean statut) {
    Serveur serveur = getServeurOuInitialiseBD();
    if (serveur == null) {
    return false;
    }
    if (statut) {
    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte SMS ACTIVE");
    } else {
    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte SMS DESACTIVE");
    }
    serveur.setEnvoialerteSMS(statut);
    return true;
    }

    public boolean activerDesactiveAlertMail(boolean statut) {
        Serveur serveur = getServeurOuInitialiseBD();
        if (serveur == null) {
            return false;
        }
        if (statut) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte MAIL ACTIVE");
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "Alerte MAIL DESACTIVE");
        }
        serveur.setEnvoieAlerteMail(statut);
        return true;
    }
     */
    /**
     * cette fonction permet de traiter les alertes issus d'une machine et de
     * relancé les alerte des taches
     *
     * @param IdMachine
     * @param listTachePB
     * @return
     */
    public boolean traitementAlerteMachine(int IdMachine, List<Integer> listTachePB) {
        Machine machine = em.find(Machine.class, IdMachine);
        if (machine == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "machine inexistante: id=" + IdMachine);
            return false;
        }
        List<Tache> listTacheMachine = getListTacheMachineByStatueTache(machine, ALERTE);
        String msg;

        for (Tache tache : listTacheMachine) {//cette boucle permet de renvoyer les msg d'alerte des taches
            msg = "Rappel d'alerte sur la machine <<" + machine.getAdresseIP() + ">> de nom <<" + tache.getNom() + ">> et de type <<" + tache.getTypeTache() + ">>";
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, msg);
            envoiMessageAlerte("rappel alerte", msg, machine.getNiveauDAlerte());
        }

        if (listTachePB.isEmpty()) {//cas ou il n'ya pas de pb sur la machine
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "communication OK avec la machine: adresse=" + machine.getAdresseIP());
        } else {
            msg = "des JOB on été redémarer sur la machine <<" + machine.getAdresseIP() + ">>";
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, msg);
            return envoiMessageAlerte("problème très anormal sur" + machine.getAdresseIP(), msg, machine.getNiveauDAlerte());
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
        Machine machine = getMachineByIP(ipAdresse);
        if (machine != null) {
            List<Tache> listTacheMachine = new ArrayList<>();
            /*List<Tache> listTache = getAllTache();
            for (Tache tache : listTache) {
            if (tache.getIdMachine().getIdMachine() == machine.getIdMachine()) {
            listTacheMachine.add(tache);
            }
            }*/
            //em.refresh(machine);
            //listTacheMachine = machine.getTacheList();
            Query query = em.createQuery("SELECT t FROM Tache t WHERE t.idMachine = :idMachine", Tache.class);
            query.setParameter("idMachine", machine);
            listTacheMachine = query.getResultList();
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches es envoyé à la machine " + ipAdresse + ": nombre de taches =" + listTacheMachine.size());
            return listTacheMachine;
        } else {
            return null;
        }
    }

    /**
     * cette fonction retourne la liste des tache avec le statue pris en
     * paramettre pour la machine pris en paramettre
     *
     * @param machine
     * @param statue represente le statue des taches qui seront retourné
     * @return
     */
    private List<Tache> getListTacheMachineByStatueTache(Machine machine, String statue) {
        Query query = em.createQuery("SELECT t FROM Tache t WHERE t.idMachine = :idMachine AND t.statue = :statue", Tache.class);
        query.setParameter("idMachine", machine);
        query.setParameter("statue", statue);
        return query.getResultList();
    }

    /*
    public List<Tache> getListTacheMachine(String ipAdresse, boolean verifiStatueTache) {
    Machine machine = getMachineByIP(ipAdresse);
    if (machine != null) {
    List<Tache> listTacheMachine = new ArrayList<>();
    
    listTacheMachine = machine.getTacheList();
    
    if (verifiStatueTache) {//si on veux verrifié le statue des taches
    if (testConnectionMachine(machine).equals(START)) {//la machine es en fonction
    for (Tache tache : listTacheMachine) {
    if (tache.getStatue().equals(START)) {//on ne fait le traitement que si le statue es START
    WSClientMonitoring ws = appelWSMachineClient(machine.getAdresseIP(), machine.getPortEcoute());
    if (ws != null) {
    if(!ws.jobExiste(tache.getIdTache()+"", tache.getIdMachine().getAdresseIP())){
    tache.setStatue(PB);//ceci es un cas anormal car le job n'es pas fonctionnel sur la machine
    }
    }
    }
    }
    } else {//le statue de la machine ne permet pas de connaitre celui des taches
    for (Tache tache : listTacheMachine) {
    if (tache.getStatue().equals(START)) {//on ne fait le traitement que si le statue es START
    tache.setStatue("inconue");
    }
    }
    }
    }
    em.clear();//permet de déconnecter l'entity manager de la BD ainsi les modification apporté au entity ne seront plus enregistré en BD
    
    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches es envoyé à la machine" + ipAdresse + ": nombre de taches=" + listTacheMachine.size());
    return listTacheMachine;
    } else {
    return null;
    }
    }
     */
    public String creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String lettre_partition, int seuil, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, lettre_partition)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, lettre_partition + ": cette partition es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_DD, description_tache, periodeVerrification, lettre_partition, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleProcessus(String adresIpMachine, String periodeVerrification, String nomProcessus, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, nomProcessus)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomProcessus + ": ce processus es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_PROCESSUS, description_tache, periodeVerrification, nomProcessus, 0, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleService(String adresIpMachine, String periodeVerrification, String nomService, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, boolean redemarer_auto_service, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, nomService)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomService + ": ce service es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_SERVICE, description_tache, periodeVerrification, nomService, 0, statue, envoiyer_alerte_mail, envoyer_alerte_sms, redemarer_auto_service, niveauDAlerte);
    }

    public String creerTachePing(String adresIpMachine, String periodeVerrification, String adresseAPinger, int nbTentative, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, adresseAPinger)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le ping vers: " + adresseAPinger + " es déja créer sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_PING, description_tache, periodeVerrification, adresseAPinger, nbTentative, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleFichierExist(String adresIpMachine, String periodeVerrification, String cheminFIchier, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, cheminFIchier)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminFIchier + ": ce fichier es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_FICHIER_EXISTE, description_tache, periodeVerrification, cheminFIchier, 0, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleTailleFichier(String adresIpMachine, String periodeVerrification, String cheminFIchier, int seuil, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, cheminFIchier)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminFIchier + ": ce fichier es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_TAILLE_FICHIER, description_tache, periodeVerrification, cheminFIchier, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheDateModificationDernierFichier(String adresIpMachine, String periodeVerrification, String cheminRepertoire, int seuil, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        if (verifiNomTacheSurMachine(adresIpMachine, cheminRepertoire)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminRepertoire + ": ce repertoire es déja surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_DATE_MODIFICATION_DERNIER_FICHIER, description_tache, periodeVerrification, cheminRepertoire, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheTelnet(String adresIpMachine, String periodeVerrification, String adresseTelnet, int port, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String adresseEtPort = adresseTelnet + "," + port;
        if (verifiNomTacheSurMachine(adresIpMachine, adresseEtPort)) {//si parmit les tache de la machine il existe déja une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le telnet vers: " + adresseEtPort + " es déja créer sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, TACHE_TELNET, description_tache, periodeVerrification, adresseEtPort, 0, statue, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    private String creerTache(String adresIpMachine, String typeTache, String description_tache, String periodeVerrification, String nom, int seuil, String statue, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, boolean redemarer_auto_service, int niveauDAlerte) {
        Machine machine = getMachineByIP(adresIpMachine);
        if (machine == null) {
            return ADRESSE_INCONU;
        }

        Tache tache = new Tache();
        tache.setTypeTache(typeTache);
        tache.setIdMachine(machine);
        tache.setSeuilAlerte(seuil);
        tache.setPeriodeVerrification(periodeVerrification);
        tache.setStatue(statue);
        tache.setDescriptionTache(description_tache);
        tache.setNom(nom);
        tache.setEnvoiyerAlerteMail(envoiyer_alerte_mail);
        tache.setEnvoyerAlerteSms(envoyer_alerte_sms);
        tache.setRedemarerAutoService(redemarer_auto_service);
        tache.setNiveauDAlerte(niveauDAlerte);
        return persist(tache);

    }

    /**
     * cette fonction permet de creer une tache appartir d'une tache pris en
     * paramettre. NB: la tache pris en paramttre ne viend pas de la BD cette
     * foction sera utilisé pour créer les tache donc les donné on été fourni à
     * l'interface graphique
     *
     * @param tache
     * @param adresseMachine
     * @return
     */
    public String creerTacheByTache(Tache tache, String adresseMachine) {
        try {
            String adresIpMachine = adresseMachine;
            String typeTache = tache.getTypeTache();
            String description_tache = tache.getDescriptionTache();
            String periodeVerrification = tache.getPeriodeVerrification();
            String nom = tache.getNom();
            int seuil = tache.getSeuilAlerte();
            String statue = tache.getStatue();
            boolean envoiyer_alerte_mail = tache.getEnvoiyerAlerteMail();
            boolean envoyer_alerte_sms = tache.getEnvoyerAlerteSms();
            boolean redemarer_auto_service = tache.getRedemarerAutoService();
            int niveauDAlerte = tache.getNiveauDAlerte();

            String resultat;

            switch (typeTache) {
                case TACHE_DD:
                    resultat = creerTacheSurveilleDD(adresIpMachine, periodeVerrification, nom, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_PROCESSUS:
                    resultat = creerTacheSurveilleProcessus(adresIpMachine, periodeVerrification, nom, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_SERVICE:
                    resultat = creerTacheSurveilleService(adresIpMachine, periodeVerrification, nom, statue, envoiyer_alerte_mail, envoyer_alerte_sms, redemarer_auto_service, description_tache, niveauDAlerte);
                    break;
                case TACHE_PING:
                    resultat = creerTachePing(adresIpMachine, periodeVerrification, nom, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_FICHIER_EXISTE:
                    resultat = creerTacheSurveilleFichierExist(adresIpMachine, periodeVerrification, nom, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_TAILLE_FICHIER:
                    resultat = creerTacheSurveilleTailleFichier(adresIpMachine, periodeVerrification, nom, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_TELNET:
                    resultat = creerTacheTelnet(adresIpMachine, periodeVerrification, nom, seuil, statue, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                default:
                    resultat = "Le type <<" + typeTache + ">> n’existe pas ";
                    Logger.getLogger(Bean.class.getName()).log(Level.WARNING, resultat);
            }
            return resultat;
        } catch (Exception e) {
            return e + "";
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

            //on teste que la connection passe effectivement
            String test = service.getWSClientMonitoringPort().hello("");
            System.out.println(test);

            return service.getWSClientMonitoringPort();
        } catch (Exception ex) {
            //Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "problème lors de l'appel du web service de la machine: " + adresse + "\n", ex);
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "problème lors de l'appel du web service de la machine: " + adresse + "\n");
            return null;
        }

    }

    /**
     * cette fonction permet de rafraichir(démarer, stopper ou redemarer) une
     * tache dans une machine physique
     *
     * @param idTache
     * @return true si les modifications ont été pris en compte dans la machine
     * physique
     */
    public boolean startRefreshStopTacheSurMachinePhy(int idTache) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        String adresse = tache.getIdMachine().getAdresseIP();
        String statueMachine = testConnectionMachine(tache.getIdMachine());
        if (!statueMachine.equals(START)) {//si la machine n'es pas en cour de fonctionnement on ne peut pas actualisé une tache deçu
            return false;
        }
        WSClientMonitoring ws = appelWSMachineClient(adresse, tache.getIdMachine().getPortEcoute());
        if (ws == null) {
            return false;//cas qui ne doit normalement pas arrivé car la fonction "testConnectionMachine" es passé
        }
        //tache.setStatue(START);
//        if (!ws.demarerMetAJourOUStopperTache(tacheServeurToTacheClient(tache))) {
        if (!ws.demarerMetAJourOUStopperTache(tache.getIdTache())) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la machine :" + adresse + " n'a pas pus actualisé la tache");
            return false;
        }
        return true;
    }


    /*
    private wsClient.Machine machineServeurToMachineClient(Machine machine) {
        if (machine == null) {
            return null;
        }
        Integer idMachine = machine.getIdMachine();
        String adresseIP = machine.getAdresseIP();
        String portEcoute = machine.getPortEcoute();
        String nomMachine = machine.getNomMachine();
        String typeOS = machine.getTypeOS();
        String periodeDeCheck = machine.getPeriodeDeCheck();
        String statue = machine.getStatue();
        //List<Tache> tacheList = machine.getTacheList();

        wsClient.Machine machineCLient = new wsClient.Machine();
        machineCLient.setIdMachine(idMachine);
        machineCLient.setAdresseIP(adresseIP);
        machineCLient.setPortEcoute(portEcoute);
        machineCLient.setNomMachine(nomMachine);
        machineCLient.setTypeOS(typeOS);
        machineCLient.setPeriodeDeCheck(periodeDeCheck);
        machineCLient.setStatue(statue);
        //machineCLient.setTacheList(tacheList);
        return machineCLient;
    }

    private wsClient.Tache tacheServeurToTacheClient(Tache tacheServeur) {
        if (tacheServeur == null) {
            return null;
        }
        Integer idTache = tacheServeur.getIdTache();
        boolean redemarerAutoService = tacheServeur.getRedemarerAutoService();
        int seuilAlerte = tacheServeur.getSeuilAlerte();
        String nom = tacheServeur.getNom();
        String statue = tacheServeur.getStatue();
        String periodeVerrification = tacheServeur.getPeriodeVerrification();
        String typeTache = tacheServeur.getTypeTache();
        boolean envoiyer_alerte_mail = tacheServeur.getEnvoiyerAlerteMail();
        boolean envoyer_alerte_sms = tacheServeur.getEnvoyerAlerteSms();
        String descriptionTache = tacheServeur.getDescriptionTache();
        Machine machine = tacheServeur.getIdMachine();

        wsClient.Tache tacheClient = new wsClient.Tache();
        tacheClient.setIdTache(idTache);
        // tacheClient.setRedemarerAutoService(redemarerAutoService);
        tacheClient.setSeuilAlerte(seuilAlerte);
        tacheClient.setNom(nom);
        tacheClient.setStatue(statue);
        tacheClient.setPeriodeVerrification(periodeVerrification);
        tacheClient.setTypeTache(typeTache);
        //tacheClient.setEnvoiyerMsgDAlerte(envoiyerMsgD_alerte);
        //tacheClient.setDescriptionTache(descriptionTache);
        tacheClient.setIdMachine(machineServeurToMachineClient(machine));

        return tacheClient;

    }

     */
    /**
     * cette fonction permet d'initialisé les paraettre de nottre serveur ou de
     * mettre à jour le serveur il n'ya une seule instance de serveur dans la
     * table serveur
     *
     * @param emailEnvoiMail
     * @param passEnvoiMail
     * @param logingSms
     * @param motDePasseSms
     * @return OK,NUMERO_COUR_INVALIDE, ECHEC_ECRITURE_BD
     */
    public String creerOuModifierServeur(String emailEnvoiMail, String passEnvoiMail, String logingSms, String motDePasseSms, String numeroCour, boolean envoiSMS, boolean envoiMail) {
        Query requete = em.createNamedQuery("Serveur.findAll", Serveur.class);
        List<Serveur> listServeur = requete.getResultList();
        Serveur serveur;
        if (listServeur == null || listServeur.size() == 0) {
            serveur = new Serveur(1);
        } else {
            serveur = listServeur.get(0);
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

    /**
     * cette fonction retourne le serveur s'il existe à défaut, il initialise la
     * BD
     *
     * @return null si on à initialisé la BD
     */
    public Serveur getServeurOuInitialiseBD() {
        Query requete = em.createNamedQuery("Serveur.findAll", Serveur.class);
        List<Serveur> listServeur = requete.getResultList();
        if (listServeur == null || listServeur.size() == 0) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "le serveur n'existe pas, il vas donc être initialisé");
            //initialisation();
            return null;
        } else {
            return listServeur.get(0);
        }

        /*
        Serveur serveur = em.find(Serveur.class, 1);
        if (serveur == null) {
        Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le serveur n'existe pas, veille initialisé la table serveur");
        }
        return serveur;
         */
    }

    /**
     * cette fontion permet d'envoyer un mail à plusieur destinataires
     *
     * @param listAdresseDestinataires
     * @param message
     * @param sujet
     */
    private boolean envoieDeMail(List<String> listAdresseDestinataires, String message, String sujet) {
        try {
            Serveur serveur = getServeurOuInitialiseBD();
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
     * cette fontion permet d'envoyer le SMS à une liste de destinataire
     *
     * @param message
     * @param destinataires
     * @return
     */
    private boolean envoieSMS(String message, List<String> destinataires) {
        Serveur serveur = getServeurOuInitialiseBD();
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
            envoieDeMail(getLlisteEmail(10), msgAlerte, "echec lors de l'envoie des SMS");//on envoi une alerte mail pour dire que l'envoi de SMS n'es pas possible
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    /**
     * cette fonction verrifie si il existe déjà un utilisateur avec un des
     * paramétres
     *
     * @param login
     * @param numero_telephone
     * @param boite_mail
     * @return true s'il existe un utilisateur avec un des paramétres
     */
    private boolean verifieExisteUtilisateur(String login, String numero_telephone, String boite_mail) {
        Query query = em.createQuery("SELECT u FROM Utilisateur u WHERE u.boiteMail = :boiteMail OR u.login = :login OR u.numeroTelephone = :numeroTelephone ", Utilisateur.class);
        query.setParameter("numeroTelephone", numero_telephone);
        query.setParameter("login", login);
        query.setParameter("boiteMail", boite_mail);
        if (!query.getResultList().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param login
     * @param pass
     * @param nom
     * @param prenom
     * @param type_compte
     * @param numero_telephone
     * @param boite_mail
     * @param niveauDAlerte
     * @return OK, INFO_DEJA_EXISTANT_EN_BD, ECHEC_ECRITURE_BD
     */
    public String creerUtilisateur(String login, String pass, String nom, String prenom, String type_compte, String numero_telephone, String boite_mail, int niveauDAlerte) {

        if (verifieExisteUtilisateur(login, numero_telephone, boite_mail)) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, INFO_DEJA_EXISTANT_EN_BD);
            return INFO_DEJA_EXISTANT_EN_BD;
        }

        /*List<Utilisateur> listUtilisateur = getAllUtilisateur();
        for (Utilisateur utilisateur : listUtilisateur) {//on verifie que les information entré n'existe pas encore dans la BD
        if (login.equalsIgnoreCase(utilisateur.getLogin())
        || numero_telephone.equalsIgnoreCase(utilisateur.getNumeroTelephone())
        || boite_mail.equalsIgnoreCase(utilisateur.getBoiteMail())) {
        Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, INFO_DEJA_EXISTANT_EN_BD);
        return INFO_DEJA_EXISTANT_EN_BD;
        }
        }*/
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setBoiteMail(boite_mail);
        utilisateur.setLogin(login);
        utilisateur.setNom(nom);
        utilisateur.setNumeroTelephone(numero_telephone);
        utilisateur.setPass(pass);
        utilisateur.setPrenom(prenom);
        utilisateur.setTypeCompte(type_compte);
        utilisateur.setNiveauDAlerte(niveauDAlerte);
        return persist(utilisateur);
    }

    /**
     * cette fonction permet d'effectué un ping à l'adresse passé en paramettre
     *
     * @param adres
     * @param nbTentative represente le nb de fois qu'on vas faire le ping
     * @return
     */
    private boolean pinger(String adres, int nbTentative) {

        int i = 0;
        boolean pingOK = false;
        while (i < nbTentative && !pingOK) {
            // System.out.println(i + ": ping à l'adresse " + adres);
            char param;
            if (OS_MACHINE.equals(OSWINDOWS)) {//on es sur une machine windows
                param = 'n';
            } else {//on es sur une machine linux
                param = 'c';
            }
            String commande = "ping -" + param + " 1 " + adres;
            List<String> resultat = executeCommand(commande);
            if (resultat == null) {
                return false;
            }
            for (String ligne : resultat) {
                if (ligne.contains("ttl=") || ligne.contains("TTL=")) {
                    pingOK = true;
                    break;
                }
            }
            i++;
        }
        //System.out.println("le nombre es: " + valeurDeRetour);
        return pingOK;

    }

    /**
     * cette fonction permet d'éxécuté la commande pris en paramettre et de
     * retourner une liste qui represente chaque ligne de la command
     *
     * @param commande
     * @return null s'il ya eu un pb
     */
    private List<String> executeCommand(String commande) {
        List<String> processes = new ArrayList<String>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(commande);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                processes.add(line);
                //  System.out.println(line);
            }
            input.close();

            //Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la commande <<" + commande + ">> c'es bien exécuté");
            return processes;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible d'exécuter la command <<" + commande + ">>\n", e);
            return null;
        }
    }

    /**
     * retourne la liste de toute les machines presente en BD avec leur status à
     * jour (INACCESSIBLE, PB_AGENT, START ou STOP) dans le cas où la machine à
     * le statue STOP, on ne verifie plus la connection avec le serveur
     *
     * @return
     */
    public List<Machine> getAllMachineAvecBonStatue() {
        List<Machine> listeMachine = getAllMachine();
        em.clear();//permet de déconnecter l'entity manager de la BD ainsi les modification apporté au entity ne seront plus enregistré en BD
        for (Machine machine : listeMachine) {
            if (!machine.getStatue().equals(STOP)) {//on met à jour le statue si le statue n'es pas STOP
                //if (machine.getStatue().equals(START)) {//on met à jour le statue si le statue es à START
                machine.setStatue(testConnectionMachine(machine));
            }
        }
        return listeMachine;
    }

    /**
     * verifie la connection entre le serveur et la machine et retourne la
     * valeur de la connection
     *
     * @param machine
     * @return INACCESSIBLE, PB_AGENT, START ou STOP
     */
    public String testConnectionMachine(Machine machine) {
        /*if (machine.getStatue().equals(STOP)) {
        return STOP;
        }*/

        if (!pinger(machine.getAdresseIP(), NB_TENTATIVE_PING_LOCAL)) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "la machine :" + machine.getAdresseIP() + "a pour status: " + INACCESSIBLE);
            return INACCESSIBLE;
        }
        WSClientMonitoring ws = appelWSMachineClient(machine.getAdresseIP(), machine.getPortEcoute());
        if (ws == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, "la machine :" + machine.getAdresseIP() + "a pour status: " + PB_AGENT);
            return PB_AGENT;
        }

        if (ws.jobExiste(machine.getIdMachine() + "", machine.getAdresseIP())) {
            //Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la machine :" + machine.getAdresseIP() + "a pour status: " + START);
            return START;
        } else {
            // Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la machine :" + machine.getAdresseIP() + "a pour status: " + STOP);
            return STOP;
        }
    }

    /**
     * cette fonction permet de demarer ou stoper ou redemarer la tache
     * principale et les sous taches d'une machine physique
     *
     * @param machine
     * @return OK, KO, INACCESSIBLE, PB_AGENT ou l'exception rencontrer
     */
    public String redemarerTachePrincipaleEtSousTache(Machine machine) {

        try {
            String resultat = testConnectionMachine(machine);
            if (resultat.equals(INACCESSIBLE) || resultat.equals(PB_AGENT)) {
                return resultat;
            }
            WSClientMonitoring ws = appelWSMachineClient(machine.getAdresseIP(), machine.getPortEcoute());
            if (ws.redemarerTachePrincipaleEtSousTache()) {
                //Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la machine physique à redémarer la tache principale et les sous taches. IP:<<"+machine.getAdresseIP()+">>");
                return OK;
            } else {
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la machine physique n'a pas pus redémarer la tache principale et les sous taches. IP:<<" + machine.getAdresseIP() + ">>");
                return KO;
            }
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, e);
            return e + "";
        }
    }

}
