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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
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
import org.apache.commons.net.telnet.TelnetClient;
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
    public static final String TACHE_UPTIME_MACHINE = "Uptime machine";
    public static final String TACHE_TEST_LIEN = "Tester lien";

    public static final String PB_AGENT = "Impossible de contacter l’agent";
    public static final String INACCESSIBLE = "Inaccessible";

    public static final String TYPE_COMPTE_SUPADMIN = "supAdmin";
    public static final String TYPE_COMPTE_ADMIN = "admin";
    public static final String DEFAUL_PERIODE_CHECK_MACHINE = "0 0 7,10,13,16,21 ? * MON-SAT";//represente la valeur par defaut de la période de check des machines 
    public static final String DEFAUL_DESCRIPTION_PERIODE_CHECK_MACHINE = "De Lun-Sam: 7H, 10H, 13H, 16H et 21H";
    public static final int NB_TENTATIVE_PING_LOCAL = 2;
    public static final int TEMP_ATTENT_TELNET_SECOND = 5;//le temps est en secomde
    public static final int NIVEAU_ALERTE = 2;
    

    public static final String TACHE_EXISTE_DEJA = "cette tache existe deja sur cette machine";
    public static final String TACHE_INEXISTANTE = "cette tache n'existe pas";
    public static final String TACHE_STOPPER = "La tâche est stoppée";
    public static final String ADRESSE_INCONU = "adresse IP inconue";
    public static final String ECHEC_ECRITURE_BD = "enregistrement dans la BD impossible";
    public static final String ADRESSE_UTILISE = "adresse ip utilise";
    public static final String NUMERO_COUR_INVALIDE = "senderID invalide";
    public static final String INFO_DEJA_EXISTANT_EN_BD = "le login ou la boite mail ou le numero de téléphone est déjà utilisé";
    public static final String ADREESSE_TELNET_INVALIDE = "Adresse Telnet invalide. l’adresse et le port doivent être séparer par un double point (:)";
    public static final String AdresseDuServerEtPort = "192.168.100.81:8080";
    
    public static final String ERREUR_PARTITION_DD = "Il n’existe pas de partition de ce type sur la machine";
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
     * @return null si ladresse ip est déjà donnée à une machine
     */
    public String creerMachine(String AdresIP, String port, String periodeCheck, String descriptionPeriode, String nonOS, String nomMachine, int niveauDAlerte) {
        Machine machine = getMachineByIP(AdresIP);
        if (machine != null) {//l'adresse ip est déjà utilisé
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, AdresIP + ": cette adresse est déjà utilisé");
            return ADRESSE_UTILISE;
        }
        machine = new Machine();
        machine.setAdresseIP(AdresIP);
        machine.setPortEcoute(port);
        machine.setNomMachine(nomMachine);
        machine.setPeriodeDeCheck(periodeCheck);
        machine.setDescriptionDeLaPeriode(descriptionPeriode);
        machine.setTypeOS(nonOS);
        machine.setNiveauDAlerte(niveauDAlerte);
        machine.setStatut(STOP);
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
    public Machine creerOuVerifiMachine(String adresIP, String port, String periodeCheck, String descriptionPeriode, String nonOS, String nomMachine) {
        Machine machine = getMachineByIP(adresIP);
        if (machine == null) {//la machine n'existe pas on la créer
            if (creerMachine(adresIP, port, periodeCheck, descriptionPeriode, nonOS, nomMachine, NIVEAU_ALERTE).equals(OK))//on créer l'objet dans la BD
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
     * est à suprimer cette fonction permet de changer le statut d'une machine
     *
     * @param id
     * @param statut si elle vaut "true" alors le statut sera START sinon elle
     * sera STOP
     * @return
     */
    /* public String changerStatutMachine(Integer id, boolean statut) {
    Machine machine = em.find(Machine.class, id);
    if (statut) {
    machine.setStatut(START);
    } else {
    machine.setStatut(STOP);
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
                if (machinePrecedente.getIdMachine() != machine.getIdMachine()) {//l'adresse ip est déjà utilisé sur une autre machine
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, adressIpMachineCourante + ": cette adresse est déjà utilisé");
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
     * retournne toute les machines donc le statut est passé en paramettre
     *
     * @param statut
     * @return
     */
    public List<Machine> getAllMachineByStatut(String statut) {
        Query query = em.createNamedQuery("Machine.findByStatut", Machine.class);
        query.setParameter("statut", statut);
        return query.getResultList();
    }

    /**
     * cette fonction verifi si la machine (donc l'adresse est pris en
     * paramettre) possède une tache avec le nom et le type pris en paramettre
     *
     * @param adresIpMachine
     * @param nomTache
     * @param typeTache
     * @return true si la machine possède ce nom de tache
     */
    public boolean verifiNomTacheSurMachine(String adresIpMachine, String nomTache, String typeTache) {

        Query query = em.createQuery("SELECT t FROM Tache t WHERE t.idMachine.adresseIP = :adresseIP AND t.nom = :nom AND t.typeTache = :typeTache", Tache.class);
        query.setParameter("adresseIP", adresIpMachine);
        query.setParameter("nom", nomTache);
        query.setParameter("typeTache", typeTache);
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
                if (verifiNomTacheSurMachine(tache.getIdMachine().getAdresseIP(), tache.getNom(), tache.getTypeTache())) {
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
     * leur niveau d'alerte est inférieur ou égal à la valeur pris en paramettre
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

    /**
     * retoune la liste de toute les boite mail
     *
     * @return
     */
    public List<String> getAllEmail() {
        List<String> listEmail = new ArrayList<>();
        Query query = em.createQuery("SELECT u.boiteMail FROM Utilisateur u", Utilisateur.class);
        listEmail = query.getResultList();
        //System.out.println("liste de tous les email");
        //for(String email:listEmail) System.out.println("-----"+email);
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
     * niveau d'alerte est inférieur ou égal à la valeur pris en paramettre
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
    

    /**
     * retourne la liste de tous les numéro de téléphone
     *
     * @return
     */
    public List<String> getAllListNumero() {
        List<String> listeNumero = new ArrayList<>();
        //listeNumero.add("237699667694");
        //listeNumero.add("237675954517");
        Query query = em.createQuery("SELECT u.numeroTelephone FROM Utilisateur u", Utilisateur.class);
        listeNumero = query.getResultList();
        /*List<Utilisateur> listUtilisateur = getAllUtilisateur();
            for (Utilisateur utilisateur : listUtilisateur) {
            listeNumero.add(utilisateur.getNumeroTelephone());
            }*/
        //System.out.println("liste de tous les numero");
        //for(String numero:listeNumero) System.out.println("+++++++"+numero);
        return listeNumero;
    }

    public boolean traitementAlerteTache(int idTache, int codeErreur) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            //Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la tache <<" + idTache + ">> n'existe pas dans la BD");
            return false;
        }
        String corpsEmail,
                //sujetEmail = "Alerte tâche (id=" + tache.getIdTache() + "): sur <<" + tache.getIdMachine().getAdresseIP() + ">> de nom <<" + tache.getNom() + ">> de type <<" + tache.getTypeTache() + ">>";
                sujetEmail = "Alerte tâche (id=" + tache.getIdTache() + ") de type \"" + tache.getTypeTache() + "\" de nom \"" + tache.getNom() + "\" sur la machine \"" + tache.getIdMachine().getAdresseIP() + "\" : " + tache.getDescriptionTache();
        switch (tache.getTypeTache()) {
            case TACHE_DD:
                if (codeErreur == 200) {//cas où la lettre de partition ne correspond à aucune partition
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " La lettre de partition ne correspond à aucune partition ou elle est invalide : <<" + tache.getNom() + " >>";
                    corpsEmail = "La lettre de partition ne correspond à aucune partition ou elle est invalide: \"" + tache.getNom() + " \"";
                } else {
                    //corpsEmailEtSMS = " espace restant du disque <<" + tache.getNom() + ">>" + "de la machine<<" + tache.getIdMachine().getAdresseIP() + ">> est faible ";
                    corpsEmail = "Espace restant du disque \"" + tache.getNom() + "\" est faible ";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_PROCESSUS:
                if (codeErreur == 0) {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le processus : <<" + tache.getNom() + " >> est arreté";
                    corpsEmail = "Le processus : \"" + tache.getNom() + " \" est arrêté";
                } else {//cas où la valeur est 1: il ya un pb
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le processus : <<" + tache.getNom() + " >> n'est pas reconnue";
                    corpsEmail = "Le processus : \"" + tache.getNom() + " \" n’est pas reconnue ou il a généré une exception";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_SERVICE:
                switch (codeErreur) {
                    case 0:
                        //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + ">> est arreté";
                        corpsEmail = "Le service : \"" + tache.getNom() + "\" est arrêté";
                        break;
                    case -1:
                        //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + " >> a été redémarer par l'agent";
                        corpsEmail = "Le service : \"" + tache.getNom() + " \" a été redémarrer par l'agent";
                        break;
                    default:
                        //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le service : <<" + tache.getNom() + " >> n'est pas reconnue";
                        corpsEmail = "Le service : \"" + tache.getNom() + " \" n'est pas reconnue";
                        break;
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_PING:
                //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le ping vers : <<" + tache.getNom() + ">> ne passe pas";
                corpsEmail = "Le Ping vers: \"" + tache.getNom() + "\" ne passe pas";
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_FICHIER_EXISTE:
                //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> n'existe pas";
                corpsEmail = "Le fichier : \"" + tache.getNom() + "\" n'existe pas";
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_TAILLE_FICHIER:
                if (codeErreur == -1) {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> n'est pas valide ou il y'a un problème inconue";
                    corpsEmail = "Le fichier : \"" + tache.getNom() + "\" n'est pas valide ou il y'a un problème inconnue";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                } else if (tache.getSeuilAlerte() < 0) {//cas où on verrifie que le fichier à surveille est toujour plus grand que le seuil
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> est inférieure à la taille autorisé: seuil=" + tache.getSeuilAlerte();
                    corpsEmail = "Le fichier : \"" + tache.getNom() + "\" est inférieure à la taille autorisé";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                } else {//cas où on verrifie que le fichier à surveille est toujour plus petit que le seuil
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le fichier : <<" + tache.getNom() + ">> est supérieure à la taille autorisé: seuil=" + tache.getSeuilAlerte();
                    corpsEmail = "Le fichier : \"" + tache.getNom() + "\" est supérieure à la taille autorisé";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                }

                break;
            case TACHE_DATE_MODIFICATION_DERNIER_FICHIER:
                if (codeErreur == -1) {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le repertoir : <<" + tache.getNom() + ">> n'est pas valide ou il y'a un problème inconue";
                    corpsEmail = "Le répertoire : \"" + tache.getNom() + "\" n'est pas valide ou il y'a un problème inconnu";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                } else {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " la date de modification du dernier fichier contenue dans le repertoire <<" + tache.getNom() + ">> n'est pas valide";
                    corpsEmail = "La date de modification du dernier fichier contenue dans le répertoire \"" + tache.getNom() + "\" n'est pas valide";
                    Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                }
                break;
            case TACHE_TELNET:
                //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le telnet vers : <<" + tache.getNom() + ">> ne passe pas";
                corpsEmail = "Le Telnet  vers : \"" + tache.getNom() + "\"ne passe pas";
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_UPTIME_MACHINE:
                if (codeErreur < 0) {
                    //corpsEmailEtSMS = "Sur la machine " + tache.getIdMachine().getAdresseIP() + ", il est impossible de connaitre le nombre de jour depuis le quelle la machine est allumer une exception c'est produit lors de l’exécution de la commande";
                    corpsEmail = "Impossible de connaitre le nombre de jour depuis le quelle la machine est allumer une exception c'est produit lors de l’exécution de la commande";
                } else {
                    //corpsEmailEtSMS = "la machine " + tache.getIdMachine().getAdresseIP() + " est alumer depuis " + codeErreur + " jours hors le seuil est de " + tache.getSeuilAlerte();
                    corpsEmail = "La machine " + tache.getIdMachine().getAdresseIP() + " est alumer depuis " + codeErreur + " jours or le seuil est de " + tache.getSeuilAlerte();
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            case TACHE_TEST_LIEN:
                if (codeErreur == 0) {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le lien vers : <<" + tache.getNom() + ">> est inaccessible ";
                    corpsEmail = "Le lien : \"" + tache.getNom() + "\" est inaccessible ";
                } else {
                    //corpsEmailEtSMS = "sur la machine: " + tache.getIdMachine().getAdresseIP() + " le test du lien : <<" + tache.getNom() + ">> a générer une exception ";
                    corpsEmail = "Le test du lien : \"" + tache.getNom() + "\" a générer une exception ";
                }
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, corpsEmail);
                break;
            default:
                corpsEmail = tache.getTypeTache() + ": le message d’alerte de ce type n’a pas encore été défini";
                Logger.getLogger(Bean.class.getName()).log(Level.WARNING, corpsEmail);
                envoiMessageAlerte(sujetEmail, corpsEmail, corpsEmail, NIVEAU_ALERTE);
                return false;
        }

        //*************envoie des messages d'alerte*********************
        tache.setStatut(ALERTE);
        envoiMessageAlertePourTache(tache, corpsEmail, sujetEmail + "\n" + corpsEmail, sujetEmail);
        return true;

    }

    /**
     * cette fonction est invoque lorsque une tache qui avais un pb ne l'a plus,
     * elle permet d'informer les administrateur qu'un problème précédament
     * signalé est de nouveau normal
     *
     * @param idTache
     * @return
     */
    public boolean problemeTacheResolu(int idTache) {
        Tache tache = getTache(idTache);
        if (tache == null) {
            return false;
        }
        //String sujetEmailEtSMS = "Situation OK pour la tâche(id=" + tache.getIdTache() + "): sur \"" + tache.getIdMachine().getAdresseIP() + "\" de nom \"" + tache.getNom() + "\" de type \"" + tache.getTypeTache() + "\" " + tache.getDescriptionTache();
        String sujetEmailEtSMS = "Situation OK pour tâche (id=" + tache.getIdTache() + ") de type \"" + tache.getTypeTache() + "\" de nom \"" + tache.getNom() + "\" sur la machine \"" + tache.getIdMachine().getAdresseIP() + "\" : " + tache.getDescriptionTache();
        String corpsEmail = "Situation OK pour la tâche";
        Logger.getLogger(Bean.class.getName()).log(Level.INFO, corpsEmail);
        tache.setStatut(START);
        envoiMessageAlertePourTache(tache, corpsEmail, sujetEmailEtSMS, sujetEmailEtSMS);
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
     * @param sujetEmail
     * @return
     */
    @Asynchronous
    private boolean envoiMessageAlertePourTache(Tache tache, String corpsEmail, String sms, String sujetEmail) {
        Serveur serveur = getServeurOuInitialiseBD();
        if (!serveur.getEnvoialerteSMS() && !serveur.getEnvoieAlerteMail()) {//cas où les msg d'alerte sont désactivé au niveau serveur (à ne pas suprimer)
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL est désactivé au niveau serveur");
            //tache.setStatut(ALERTE);
            return true;
        }
        if (!tache.getEnvoiyerAlerteMail() && !tache.getEnvoyerAlerteSms()) {//cas où les msg d'alerte sont désactivé au niveau de la tache (à ne pas suprimer)
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL est désactivé au niveau de la tache");
            //tache.setStatut(ALERTE);
            return true;
        }

        boolean traiter = false;
        if (serveur.getEnvoieAlerteMail()) {
            if (tache.getEnvoiyerAlerteMail()) {
                if (envoieDeMail(getLlisteEmail(tache.getNiveauDAlerte()), getTabHTML(tache, corpsEmail), sujetEmail)) {
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
                if (envoieSMS(sms, getListNumero(tache.getNiveauDAlerte()), true)) {
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
     * @param corpsEmail
     * @param sms
     * @param niveauDAlert
     * @return
     */
    public boolean envoiMessageAlerte(String sujetMail, String corpsEmail, String sms, int niveauDAlert) {
        //envoie de l'alerte aux administrateur (si les mail ou les SMS on pus être envoyer alors le problème à été traité)
        Serveur serveur = getServeurOuInitialiseBD();
        if (!serveur.getEnvoialerteSMS() && !serveur.getEnvoieAlerteMail()) {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alerte SMS et MAIL est désactivé");
            return true;
        }
        boolean envoiMail = false;
        if (serveur.getEnvoieAlerteMail()) {
            envoiMail = envoieDeMail(getLlisteEmail(niveauDAlert), corpsEmail, sujetMail);
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes mail est désactivé");
        }
        boolean envoiSMS = false;
        if (serveur.getEnvoialerteSMS()) {
            envoiSMS = envoieSMS(sms, getListNumero(niveauDAlert), true);
        } else {
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "l'envoie des alertes SMS est désactivé");
        }

        if (envoiMail || envoiSMS) {
            return true;
        } else {//cas où aucun msg d'alert n'a pus être envoyé
            return true;
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
        List<Tache> listTacheMachine = getListTacheMachineByStatutTache(machine, ALERTE);
        String msg;

        for (Tache tache : listTacheMachine) {//cette boucle permet de renvoyer les msg d'alerte des taches
            //String sujetEmailEtSMS = "Rappel Alerte Tache(id=" + tache.getIdTache() + "): sur \"" + tache.getIdMachine().getAdresseIP() + "\" de nom \"" + tache.getNom() + "\" de type \"" + tache.getTypeTache() + "\" "+tache.getDescriptionTache();
            String sujetEmailEtSMS = "Rappel alerte tâche (id=" + tache.getIdTache() + ") de type \"" + tache.getTypeTache() + "\" de nom \"" + tache.getNom() + "\" sur la machine \"" + tache.getIdMachine().getAdresseIP() + "\" : " + tache.getDescriptionTache();
            msg = getTabHTML(tache, "Rappel alerte tâche");
            Logger.getLogger(Bean.class.getName()).log(Level.WARNING, msg);
            envoiMessageAlerte(sujetEmailEtSMS, msg, sujetEmailEtSMS, machine.getNiveauDAlerte());
        }

        if (listTachePB.isEmpty()) {//cas ou il n'ya pas de pb sur la machine
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "communication OK avec la machine: adresse=" + machine.getAdresseIP());
        } else {
            msg = "des JOB on été redémarer sur la machine \"" + machine.getAdresseIP() + "\"";
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, msg);
            return envoiMessageAlerte("problème très anormal sur" + machine.getAdresseIP(), msg, msg, machine.getNiveauDAlerte());
        }
        return true;
    }

    /**
     * retourne la liste des tache d'une machine donc l'adresse est prise en
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
            Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches est envoyé à la machine " + ipAdresse + ": nombre de taches =" + listTacheMachine.size());
            return listTacheMachine;
        } else {
            return null;
        }
    }

    /**
     * cette fonction retourne la liste des tache avec le statut pris en
     * paramettre pour la machine pris en paramettre
     *
     * @param machine
     * @param statut represente le statut des taches qui seront retourné
     * @return
     */
    private List<Tache> getListTacheMachineByStatutTache(Machine machine, String statut) {
        Query query = em.createQuery("SELECT t FROM Tache t WHERE t.idMachine = :idMachine AND t.statut = :statut", Tache.class);
        query.setParameter("idMachine", machine);
        query.setParameter("statut", statut);
        return query.getResultList();
    }

    /*
    public List<Tache> getListTacheMachine(String ipAdresse, boolean verifiStatutTache) {
    Machine machine = getMachineByIP(ipAdresse);
    if (machine != null) {
    List<Tache> listTacheMachine = new ArrayList<>();
    
    listTacheMachine = machine.getTacheList();
    
    if (verifiStatutTache) {//si on veux verrifié le statut des taches
    if (testConnectionMachine(machine).equals(START)) {//la machine est en fonction
    for (Tache tache : listTacheMachine) {
    if (tache.getStatut().equals(START)) {//on ne fait le traitement que si le statut est START
    WSClientMonitoring ws = appelWSMachineClient(machine.getAdresseIP(), machine.getPortEcoute());
    if (ws != null) {
    if(!ws.jobExiste(tache.getIdTache()+"", tache.getIdMachine().getAdresseIP())){
    tache.setStatut(PB);//ceci est un cas anormal car le job n'est pas fonctionnel sur la machine
    }
    }
    }
    }
    } else {//le statut de la machine ne permet pas de connaitre celui des taches
    for (Tache tache : listTacheMachine) {
    if (tache.getStatut().equals(START)) {//on ne fait le traitement que si le statut est START
    tache.setStatut("inconue");
    }
    }
    }
    }
    em.clear();//permet de déconnecter l'entity manager de la BD ainsi les modification apporté au entity ne seront plus enregistré en BD
    
    Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la liste des Taches est envoyé à la machine" + ipAdresse + ": nombre de taches=" + listTacheMachine.size());
    return listTacheMachine;
    } else {
    return null;
    }
    }
     */
    public String creerTacheSurveilleDD(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String lettre_partition, int seuil, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_DD;
        if (verifiNomTacheSurMachine(adresIpMachine, lettre_partition, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, lettre_partition + ": cette partition est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, lettre_partition, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheUptimeMachine(String adresIpMachine, String periodeVerrification, String descriptionPeriode, int seuil, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String nomUptime = "redémarrage";
        String typeTache = TACHE_UPTIME_MACHINE;
        if (verifiNomTacheSurMachine(adresIpMachine, nomUptime, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "la tâche " + nomUptime + " existe déjà sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, nomUptime, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleProcessus(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String nomProcessus, int nbTentative, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_PROCESSUS;
        if (verifiNomTacheSurMachine(adresIpMachine, nomProcessus, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomProcessus + ": ce processus est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, nomProcessus, nbTentative, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleService(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String nomService, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, boolean redemarer_auto_service, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_SERVICE;
        if (verifiNomTacheSurMachine(adresIpMachine, nomService, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, nomService + ": ce service est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, nomService, 0, statut, envoiyer_alerte_mail, envoyer_alerte_sms, redemarer_auto_service, niveauDAlerte);
    }

    public String creerTachePing(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String adresseAPinger, int nbTentative, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_PING;
        if (verifiNomTacheSurMachine(adresIpMachine, adresseAPinger, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le ping vers: " + adresseAPinger + " est déjà créer sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, adresseAPinger, nbTentative, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheTestLien(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String lien, int nbTentative, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_TEST_LIEN;
        if (verifiNomTacheSurMachine(adresIpMachine, lien, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le Test du lien : " + lien + " est déjà créer sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, lien, nbTentative, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleFichierExist(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String cheminFIchier, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_FICHIER_EXISTE;
        if (verifiNomTacheSurMachine(adresIpMachine, cheminFIchier, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminFIchier + ": ce fichier est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, cheminFIchier, 0, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheSurveilleTailleFichier(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String cheminFIchier, int seuil, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_TAILLE_FICHIER;
        if (verifiNomTacheSurMachine(adresIpMachine, cheminFIchier, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminFIchier + ": ce fichier est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, cheminFIchier, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheDateModificationDernierFichier(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String cheminRepertoire, int seuil, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        String typeTache = TACHE_DATE_MODIFICATION_DERNIER_FICHIER;
        if (verifiNomTacheSurMachine(adresIpMachine, cheminRepertoire, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, cheminRepertoire + ": ce repertoire est déjà surveillé sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, cheminRepertoire, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    public String creerTacheTelnet(String adresIpMachine, String periodeVerrification, String descriptionPeriode, String adresseEtPort, int nbTentative, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, String description_tache, int niveauDAlerte) {
        // String adresseEtPort = adresseTelnet + "," + port;
        String typeTache = TACHE_TELNET;
        if (!adresseEtPort.contains(":")) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "l'adresse pour le telnet est invalide: " + adresseEtPort + " l'adresse et le port doivent être séparer par un double point (:) ");
            return ADREESSE_TELNET_INVALIDE;
        }
        if (verifiNomTacheSurMachine(adresIpMachine, adresseEtPort, typeTache)) {//si parmit les tache de la machine il existe déjà une taches ayant ce nom on ne créer plus la tache
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "le telnet vers: " + adresseEtPort + " est déjà créer sur la machine: " + adresIpMachine);
            return TACHE_EXISTE_DEJA;
        }
        return creerTache(adresIpMachine, typeTache, description_tache, periodeVerrification, descriptionPeriode, adresseEtPort, nbTentative, statut, envoiyer_alerte_mail, envoyer_alerte_sms, false, niveauDAlerte);
    }

    private String creerTache(String adresIpMachine, String typeTache, String description_tache, String periodeVerrification,String descriptionPeriode, String nom, int seuil, String statut, boolean envoiyer_alerte_mail, boolean envoyer_alerte_sms, boolean redemarer_auto_service, int niveauDAlerte) {
        Machine machine = getMachineByIP(adresIpMachine);
        if (machine == null) {
            return ADRESSE_INCONU;
        }

        Tache tache = new Tache();
        tache.setTypeTache(typeTache);
        tache.setIdMachine(machine);
        tache.setSeuilAlerte(seuil);
        tache.setPeriodeVerrification(periodeVerrification);
        tache.setDescriptionDeLaPeriode(descriptionPeriode);
        tache.setStatut(statut);
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
            String descriptionPeriode = tache.getDescriptionDeLaPeriode();
            String nom = tache.getNom();
            int seuil = tache.getSeuilAlerte();
            String statut = tache.getStatut();
            boolean envoiyer_alerte_mail = tache.getEnvoiyerAlerteMail();
            boolean envoyer_alerte_sms = tache.getEnvoyerAlerteSms();
            boolean redemarer_auto_service = tache.getRedemarerAutoService();
            int niveauDAlerte = tache.getNiveauDAlerte();

            String resultat;

            switch (typeTache) {
                case TACHE_DD:
                    resultat = creerTacheSurveilleDD(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_PROCESSUS:
                    resultat = creerTacheSurveilleProcessus(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_SERVICE:
                    resultat = creerTacheSurveilleService(adresIpMachine, periodeVerrification, descriptionPeriode, nom, statut, envoiyer_alerte_mail, envoyer_alerte_sms, redemarer_auto_service, description_tache, niveauDAlerte);
                    break;
                case TACHE_PING:
                    resultat = creerTachePing(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_FICHIER_EXISTE:
                    resultat = creerTacheSurveilleFichierExist(adresIpMachine, periodeVerrification, descriptionPeriode, nom, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_TAILLE_FICHIER:
                    resultat = creerTacheSurveilleTailleFichier(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_TELNET:
                    resultat = creerTacheTelnet(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_DATE_MODIFICATION_DERNIER_FICHIER:
                    resultat = creerTacheDateModificationDernierFichier(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_UPTIME_MACHINE:
                    resultat = creerTacheUptimeMachine(adresIpMachine, periodeVerrification, descriptionPeriode, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
                    break;
                case TACHE_TEST_LIEN:
                    resultat = creerTacheTestLien(adresIpMachine, periodeVerrification, descriptionPeriode, nom, seuil, statut, envoiyer_alerte_mail, envoyer_alerte_sms, description_tache, niveauDAlerte);
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
     * permet de faire le telnet. il sera util pour verrifié que la connection
     * avec les agent est OK
     *
     * @return
     */
    private boolean telnet(String adresse, int port) {
        try {
            TelnetClient telnet = new TelnetClient();
            telnet.setConnectTimeout(TEMP_ATTENT_TELNET_SECOND * 1000);//On converti le temps d'attente en second
            telnet.connect(adresse, port);
            if (telnet.isConnected()) {
                telnet.disconnect();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * cette fonction permet d'ouvrire une connection web service vers une
     * machine qu'on supervise
     */
    private WSClientMonitoring appelWSMachineClient(String adresse, String port) {
        try {
            //on teste que la connection avec l'agent
            if (!telnet(adresse, new Integer(port))) {
                Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible de contacter l'agent de la machine: " + adresse + "\n");
                return null;
            }
            /*if (service.getExecutor() == null) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible de contacter l'agent de la machine: " + adresse + "\n");
            return null;
            }*/

            //String adresse = "172.16.4.2";
            //String port = "8088";
            URL url = new URL("http://" + adresse + ":" + port + "/WSClientMonitoring?wsdl");
            wsClient.WSClientMonitoringService service = new wsClient.WSClientMonitoringService(url);

            return service.getWSClientMonitoringPort();
        } catch (Exception ex) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "problème lors de l'appel du web service de la machine: " + adresse + "\n", ex);
            //Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "problème lors de l'appel du web service de la machine: " + adresse + "\n");
            return null;
        }

    }

    /**
     * cette fonction permet de rafraichir(démarer, stopper ou redemarer) une
     * tache dans une machine physique. elle agit sur la machine physique si
     * celle-ci n'est pas sur STOP
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
        Machine machine = tache.getIdMachine();
        if (machine.getStatut().equals(STOP)) {
            return false;
        }
        String adresse = machine.getAdresseIP();
        String statutMachine = testConnectionMachine(machine);
        if (!statutMachine.equals(START)) {//si la machine n'est pas en cours de fonctionnement on ne peut pas actualisé une tache deçu
            return false;
        }
        WSClientMonitoring ws = appelWSMachineClient(adresse, tache.getIdMachine().getPortEcoute());
        if (ws == null) {
            return false;//ne doit normalement pas arrivé car la fonction "testConnectionMachine" est passé
        }
        //tache.setStatut(START);
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
        String statut = machine.getStatut();
        //List<Tache> tacheList = machine.getTacheList();

        wsClient.Machine machineCLient = new wsClient.Machine();
        machineCLient.setIdMachine(idMachine);
        machineCLient.setAdresseIP(adresseIP);
        machineCLient.setPortEcoute(portEcoute);
        machineCLient.setNomMachine(nomMachine);
        machineCLient.setTypeOS(typeOS);
        machineCLient.setPeriodeDeCheck(periodeDeCheck);
        machineCLient.setStatut(statut);
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
        String statut = tacheServeur.getStatut();
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
        tacheClient.setStatut(statut);
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
    public boolean envoieDeMail(List<String> listAdresseDestinataires, String message, String sujet) {
        
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
            mailMessage.setContent(message, "text/html; charset=utf-8");
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
     * cette fontion permet d'envoyer le SMS à une liste de destinataire.
     *
     * @param message
     * @param destinataires
     * @param envoiMailPB permet de dire s'il faut envoyer un mail pour dire que
     * le sms n'a pas pus être envoyé
     * @return
     */
    public boolean envoieSMS(String message, List<String> destinataires, boolean envoiMailPB) {
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
                    msgAlerte = "Pas assez de crédit SMS";
                    break;
                case 404:
                    msgAlerte = " login ou mot de passe LMT non valide";
                    break;

                default:
                    msgAlerte = "erreur inconue lors de l'envoie du SMS";
                    break;
            }

            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, msgAlerte);
            if (envoiMailPB) {
                envoieDeMail(getAllEmail(), msgAlerte, "Échec lors de l'envoie");//on envoi une alerte mail pour dire que l'envoi de SMS n'est pas possible
            }
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
    public boolean pinger(String adres, int nbTentative) {
        System.out.println("ping à l'adresse " + adres);
        int i = 0;
        //boolean pingOK = false;
        do {
            // System.out.println(i + ": ping à l'adresse " + adres);
            char param;
            if (OS_MACHINE.equals(OSWINDOWS)) {//on est sur une machine windows
                param = 'n';
            } else {//on est sur une machine linux
                param = 'c';
            }
            String commande = "ping -" + param + " 1 " + adres;
            List<String> resultat = executeCommand(commande);
            if (resultat == null) {
                return false;
            }
            for (String ligne : resultat) {
                if (ligne.contains("ttl=") || ligne.contains("TTL=")) {
                    return true;
                }
            }
            i++;
            //System.out.println(i + "- tentative ping à l'adresse " + adres);
        } while (i < nbTentative );
        //System.out.println("le nombre es: " + valeurDeRetour);
        return false;

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

            //Logger.getLogger(Bean.class.getName()).log(Level.INFO, "la commande <<" + commande + ">> c'est bien exécuté");
            return processes;
        } catch (Exception e) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, "impossible d'exécuter la command <<" + commande + ">>\n", e);
            return null;
        }
    }

    /**
     * retourne la liste de toute les machines presente en BD avec leur status à
     * jour (INACCESSIBLE, PB_AGENT, START ou STOP) dans le cas où la machine à
     * le statut STOP, on ne verifie plus la connection avec le serveur
     *
     * @return
     */
    public List<Machine> getAllMachineAvecBonStatut() {
        List<Machine> listeMachine = getAllMachine();
        em.clear();//permet de déconnecter l'entity manager de la BD ainsi les modification apporté au entity ne seront plus enregistré en BD
        for (Machine machine : listeMachine) {
            if (!machine.getStatut().equals(STOP)) {//on met à jour le statut si le statut n'est pas STOP
                //if (machine.getStatut().equals(START)) {//on met à jour le statut si le statut est à START
                machine.setStatut(testConnectionMachine(machine));
            }
        }
        return listeMachine;
    }

    public Machine getMachineAvecBonStatut(String adresse) {
        Machine machine = getMachineByIP(adresse);
        em.clear();
        if (!machine.getStatut().equals(STOP)) {//on met à jour le statut si le statut n'est pas STOP
            //if (machine.getStatut().equals(START)) {//on met à jour le statut si le statut est à START
            machine.setStatut(testConnectionMachine(machine));
        }
        return machine;
    }

    /**
     * verifie la connection entre le serveur et la machine et retourne la
     * valeur de la connection
     *
     * @param machine
     * @return INACCESSIBLE, PB_AGENT, START ou STOP
     */
    public String testConnectionMachine(Machine machine) {
        /*if (machine.getStatut().equals(STOP)) {
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
        System.out.println(ws.hello(""));

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

    /**
     * cette fonction donne la possibilité d'exécuter la tache sans verrifiè la
     * date de reveille de la tâche. Ainsi lorsque cette fonction est appeler la
     * tâches vas être exécuté à l'instant
     *
     * @param tache
     * @return
     */
    public String executerTache(Tache tache) {
        if (tache.getStatut().equals(Bean.STOP)) {
            return TACHE_STOPPER;
        }
        String statutMachine = testConnectionMachine(tache.getIdMachine());
        if (!statutMachine.equals(START)) {
            return statutMachine;
        }
        WSClientMonitoring ws = appelWSMachineClient(tache.getIdMachine().getAdresseIP(), tache.getIdMachine().getPortEcoute());
        if (ws.executeJob(tache.getIdTache(), tache.getIdMachine().getIdMachine())) {
            return OK;
        } else {
            return KO;
        }
    }

    /**
     * cette fonction permet de tester une tache. Elle permet de retourné ce qui
     * permet à la tâche de fonctionné
     *
     * @param tache cette tache doit existé en base de données
     * @return
     */
    public String testTache(Tache tache) {
        WSClientMonitoring ws = appelWSMachineClient(tache.getIdMachine().getAdresseIP(), tache.getIdMachine().getPortEcoute());
        if (ws == null) {
            return null;
        }
        return ws.testTache(tache.getIdTache());
    }

    /**
     * cette fonction permet de tester la communication avec un agent. il permet
     * aussi de connaitre la version de l'agents
     *
     * @param machine
     * @return
     */
    public String testAgent(Machine machine) {
        WSClientMonitoring ws = appelWSMachineClient(machine.getAdresseIP(), machine.getPortEcoute());
        if (ws == null) {
            return null;
        }
        return ws.hello("");
    }

    /**
     * cette fonction retourne un table HTML qui represente une tâche
     *
     * @param tache
     * @param msg
     * @return
     */
    private String getTabHTML(Tache tache, String msg) {
        if (tache == null) {
            return null;
        }
        return "<TABLE BORDER CELLPADDING=10 CELLSPACING=0>"
                + "	<TR>"
                + "		<TD>Message</TD> <TD>" + msg + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Machine hôte</TD> <TD>" + tache.getIdMachine().getAdresseIP() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Type</TD> <TD>" + tache.getTypeTache() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Nom</TD> <TD>" + tache.getNom() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Date</TD> <TD>" + new Date() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Description</TD> <TD>" + tache.getDescriptionTache() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Statut</TD> <TD>" + tache.getStatut() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>Seuil</TD> <TD>" + tache.getSeuilAlerte() + "</TD> "
                + "	</TR>"
                + "	<TR>"
                + "		<TD>ID</TD> <TD>" + tache.getIdTache() + "</TD> "
                + "	</TR>"
                + "</TABLE>"
                + "<a href=\"http://"+AdresseDuServerEtPort+"/projetMonitoring-war/\">Interface de monitoring</a>";
                //+ "<a href=\"http://"+AdresseDuServerEtPort+"/projetMonitoring-war/faces/listTachesMachine.xhtml?adresseMachine="+tache.getIdMachine().getAdresseIP()+"\">Interface de monitoring de la "+tache.getIdMachine().getAdresseIP()+"</a>";
    }
}
