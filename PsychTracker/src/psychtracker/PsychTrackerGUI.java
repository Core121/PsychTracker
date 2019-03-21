/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package psychtracker;

import Classes.Client;
import Classes.Figures;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Corey
 */
public class PsychTrackerGUI extends javax.swing.JFrame {
    private int index = -1;
    private int selectedFigure = -1;
    private int currentNote;
    private String ciph;
    private String username;
    private ArrayList<Client> clients = new ArrayList<>();
    
    //only run at very beginning
    protected void SetUpLoginScenario() {
        this.PsychTrackerTabs.addTab("Login", LoginTab);
        this.PsychTrackerTabs.addTab("Sign Up", SignUpTab);
        this.PsychTrackerTabs.remove(this.ClientTab);
        this.PsychTrackerTabs.remove(this.NotesTab);
        this.PsychTrackerTabs.remove(this.FiguresTab);
    }
    
    private void SetUpNormalUserScenario() {
        this.PsychTrackerTabs.remove(this.LoginTab);
        this.PsychTrackerTabs.remove(this.SignUpTab);
        this.PsychTrackerTabs.addTab("Client", new ImageIcon(this.getClass().getResource("/Res/ClientImg.png")), this.ClientTab);
        this.PsychTrackerTabs.addTab("Figures", FiguresTab);
        this.PsychTrackerTabs.addTab("Notes", NotesTab);
    }
    
    
    
    private void refreshClientListFName() {
        if(clients.size()>0){
        String[] array = new String[clients.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = clients.get(i).getFirstname();
        }
        this.clientComboBox.setModel(new DefaultComboBoxModel(array));
        index = this.clientComboBox.getSelectedIndex();
        this.LoadInAllClientData(clients.get(index));
        }
    }
    
    private void refreshFigureList() {
        DefaultListModel list = new DefaultListModel();
        if (clients.get(index).getFigures().size() > 0) {
            for (int i = 0; i < clients.get(index).getFigures().size(); i++) {
                list.add(i, clients.get(index).getFigures().get(i).getFirstname() + " " + clients.get(index).getFigures().get(i).getLastname() +": " + clients.get(index).getFigures().get(i).getRelationship());
            }
        }
        else{
            this.fnamefieldfigure.setText("");
            this.lnamefieldfigure.setText("");
            this.emailfieldfigure.setText("");
            this.phonefieldfigure.setText("");
            this.relationshipfigurefield.setText("");
            this.ratingfieldfigure.setText("");
            this.figurenotetextarea.setText("");
            selectedFigure = -1;
        }
        this.figurelist.setModel(list);
    }
    
    private void FillinClientTab(){
        this.fnamefield.setText(clients.get(index).getFirstname());
        this.lnamefield.setText(clients.get(index).getLastname());
        if(clients.get(index).getEmail()!=""){
        this.emailfield.setText(clients.get(index).getEmail());
        }
        else{
            this.emailfield.setText("");
        }
        if(!clients.get(index).getPhonenumber().equals(new BigInteger("0"))){
        this.phonefield.setText(clients.get(index).getPhonenumber().toString());
        }
        else{
            this.phonefield.setText("");
        }
    }
    
    private void FillinFigureTab(){
        this.fnamefieldfigure.setText(clients.get(index).getFigures().get(selectedFigure).getFirstname());
        this.lnamefieldfigure.setText(clients.get(index).getFigures().get(selectedFigure).getLastname());
        this.figurenotetextarea.setText(clients.get(index).getFigures().get(selectedFigure).getNote());
        this.relationshipfigurefield.setText(clients.get(index).getFigures().get(selectedFigure).getRelationship());
        this.ratingfieldfigure.setText(Integer.toString(clients.get(index).getFigures().get(selectedFigure).getRating()));
        if (clients.get(index).getFigures().get(selectedFigure).getEmail() != "") {
            this.emailfield.setText(clients.get(index).getFigures().get(selectedFigure).getEmail());
        } else {
            this.emailfield.setText("");
        }
        if(!clients.get(index).getPhonenumber().equals(new BigInteger("0"))){
            this.phonefieldfigure.setText(clients.get(index).getFigures().get(selectedFigure).getPhonenumber().toString());
        } else {
            this.phonefieldfigure.setText("");
        }
        if(clients.get(index).getFigures().get(selectedFigure).getRating()!=-1){
            this.ratingfieldfigure.setText(Integer.toString(clients.get(index).getFigures().get(selectedFigure).getRating()));
        }
        else{
            this.ratingfieldfigure.setText("");
        }
    }
    
    private void LoadInAllClientData(Client client){
        this.refreshNotes(client);
        this.FillinClientTab();
        this.refreshFigureList();
    }
    
    private void refreshNotes(Client client){
        if(!client.getNotes().isEmpty()){
            int size = client.getNotes().size();
            this.NoteTextArea.setText(client.getNotes().get(size-1).getNote());
            this.DateLabel.setText(client.getNotes().get(size-1).getDate().getMonth()+1 +"/"+ client.getNotes().get(size-1).getDate().getDate()+"/"+client.getNotes().get(size-1).getDate().getYear());
            currentNote = size-1;
        }
        else{
            this.NoteTextArea.setText("");
            this.DateLabel.setText("00/00/00");
            this.EditNoteButton.setEnabled(false);
        }
    }
    
    
    private void EncryptFile() throws ClassNotFoundException{
        try {
            String path = System.getProperty("user.home") + File.separator + "Documents";
            path += File.separator + "PsychTracker" + File.separator+ this.username + ".dat";
            File customFile = new File(path);

            SecretKey key64 = new SecretKeySpec(ciph.getBytes(), "Blowfish");
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("Blowfish");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            cipher.init(Cipher.ENCRYPT_MODE, key64);
            SealedObject sealedObject = new SealedObject(this.clients, cipher);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(customFile)), cipher);
            ObjectOutputStream outputStream = new ObjectOutputStream(cipherOutputStream);
            
            outputStream.writeObject(sealedObject);
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void DecrpytFile() throws InvalidKeyException{
         String path = System.getProperty("user.home") + File.separator + "Documents";
            path += File.separator + "PsychTracker" + File.separator + this.username + ".dat";
            File customFile = new File(path);
//You may use any combination, but you should use the same for writing and reading
            SecretKey key64 = new SecretKeySpec(ciph.getBytes(), "Blowfish");
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("Blowfish");
                cipher.init(Cipher.DECRYPT_MODE, key64);
            CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(new FileInputStream(customFile)), cipher);
            ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) inputStream.readObject();
            ArrayList<Client> passClients = (ArrayList<Client>) sealedObject.getObject(cipher);
            this.clients = passClients;
            this.SetUpNormalUserScenario();
            this.refreshClientListFName();
            }catch(StreamCorruptedException e){
                JOptionPane.showMessageDialog(null, "Wrong password!", "Error",JOptionPane.ERROR_MESSAGE);
                    }
            catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
            catch (ClassNotFoundException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public void refreshClientListLName() {
        if(clients.size()>0){
        String[] array = new String[clients.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = clients.get(i).getLastname();
        }
        this.clientComboBox.setModel(new DefaultComboBoxModel(array));
        index = this.clientComboBox.getSelectedIndex();
        this.LoadInAllClientData(clients.get(index));
        }
    }
    
    public void sortClientsbyFirstName(ArrayList<Client> clients) {
        boolean flag = true;  // will determine when the sort is finished
        Client temp;

        while (flag) {
            flag = false;
            for (int i = 0; i < clients.size() - 1; i++) {
                if (clients.get(i).getFirstname().compareToIgnoreCase(clients.get(i + 1).getFirstname()) > 0) {                                             // ascending sort
                    temp = clients.get(i);
                    clients.set(i, clients.get(i + 1));    // swapping
                    clients.set(i + 1, temp);
                    flag = true;
                }
            }
        }
    }

    public void sortClientsByLastName(ArrayList<Client> clients) {
        boolean flag = true;  // will determine when the sort is finished
        Client temp;

        while (flag) {
            flag = false;
            for (int i = 0; i < clients.size() - 1; i++) {
                if (clients.get(i).getLastname().compareToIgnoreCase(clients.get(i + 1).getLastname()) > 0) {                                             // ascending sort
                    temp = clients.get(i);
                    clients.set(i, clients.get(i + 1));    // swapping
                    clients.set(i + 1, temp);
                    flag = true;
                }
            }
        }
    }
    

    public PsychTrackerGUI() {
        initComponents();
        this.addWindowListener(new WindowAdapter() {
            //I skipped unused callbacks for readability

            @Override
            public void windowClosing(WindowEvent e) {
                    setVisible(false);
                    if(username!=null && ciph!=null){
                        try {
                            EncryptFile();
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    dispose();
                    System.exit(0);
            }
        });
        this.DoneNoteButton.setVisible(false);
        this.DoneEditNoteButton.setVisible(false);
        this.donebuttonclientfigure.setVisible(false);
        this.donebuttonclient.setVisible(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Res/brainstorm.png")));
        this.setExtendedState(this.MAXIMIZED_BOTH);
        this.SetUpLoginScenario();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SortButtonGroup = new javax.swing.ButtonGroup();
        TopPanel = new javax.swing.JPanel();
        LionStudyImage = new javax.swing.JLabel();
        LionStudyText = new javax.swing.JLabel();
        pennstatelogolabel = new javax.swing.JLabel();
        PsychTrackerTabs = new javax.swing.JTabbedPane();
        ClientTab = new javax.swing.JPanel();
        clientPanel = new javax.swing.JPanel();
        clientComboBox = new javax.swing.JComboBox<String>();
        clientLabel = new javax.swing.JLabel();
        addClientButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        fnameradio = new javax.swing.JRadioButton();
        lanmeradio = new javax.swing.JRadioButton();
        FiguresPanel = new javax.swing.JPanel();
        namelabel = new javax.swing.JLabel();
        namelabel1 = new javax.swing.JLabel();
        namelabel2 = new javax.swing.JLabel();
        namelabel3 = new javax.swing.JLabel();
        editButton = new javax.swing.JButton();
        fnamefield = new javax.swing.JTextField();
        emailfield = new javax.swing.JTextField();
        lnamefield = new javax.swing.JTextField();
        phonefield = new javax.swing.JTextField();
        donebuttonclient = new javax.swing.JButton();
        LoginTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        LoginButton = new javax.swing.JButton();
        PasswordText = new javax.swing.JLabel();
        passwordfield = new javax.swing.JPasswordField();
        UsernameText = new javax.swing.JLabel();
        usernamefield = new javax.swing.JTextField();
        LionStudyLoginText = new javax.swing.JLabel();
        SignUpTab = new javax.swing.JPanel();
        SignUpPanel = new javax.swing.JPanel();
        singuppasswordlabel = new javax.swing.JLabel();
        signupusernamelabel = new javax.swing.JLabel();
        passwordfieldsignup = new javax.swing.JPasswordField();
        usernamefieldsignup = new javax.swing.JTextField();
        SignUp = new javax.swing.JButton();
        reenterpasslabel = new javax.swing.JLabel();
        passwordfieldsignupreenter = new javax.swing.JPasswordField();
        LionStudySignUpLabel = new javax.swing.JLabel();
        FiguresTab = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        figurelist = new javax.swing.JList();
        fnamefieldfigure = new javax.swing.JTextField();
        namelabel4 = new javax.swing.JLabel();
        namelabel5 = new javax.swing.JLabel();
        lnamefieldfigure = new javax.swing.JTextField();
        emailfieldfigure = new javax.swing.JTextField();
        namelabel6 = new javax.swing.JLabel();
        namelabel7 = new javax.swing.JLabel();
        phonefieldfigure = new javax.swing.JTextField();
        editButtonfigure = new javax.swing.JButton();
        donebuttonclientfigure = new javax.swing.JButton();
        namelabel8 = new javax.swing.JLabel();
        ratingfieldfigure = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        figurenotetextarea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        namelabel9 = new javax.swing.JLabel();
        relationshipfigurefield = new javax.swing.JTextField();
        CreateFigureButton = new javax.swing.JButton();
        NotesTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        NoteTextArea = new javax.swing.JTextArea();
        DateLabel = new javax.swing.JLabel();
        EditNoteButton = new javax.swing.JButton();
        CreateNoteButton = new javax.swing.JButton();
        BackwardNoteButton = new javax.swing.JButton();
        ForwardNoteButton = new javax.swing.JButton();
        DoneNoteButton = new javax.swing.JButton();
        DoneEditNoteButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PsychTracker");
        setMaximumSize(null);

        TopPanel.setBackground(new java.awt.Color(6, 6, 50));
        TopPanel.setForeground(new java.awt.Color(255, 255, 255));

        LionStudyImage.setBackground(new java.awt.Color(0, 0, 51));

        LionStudyText.setFont(new java.awt.Font("Arabic Typesetting", 0, 48)); // NOI18N
        LionStudyText.setForeground(new java.awt.Color(244, 244, 238));
        LionStudyText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LionStudyText.setText("PsychTracker");
        LionStudyText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        pennstatelogolabel.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout TopPanelLayout = new javax.swing.GroupLayout(TopPanel);
        TopPanel.setLayout(TopPanelLayout);
        TopPanelLayout.setHorizontalGroup(
            TopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, TopPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LionStudyText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LionStudyImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pennstatelogolabel))
        );
        TopPanelLayout.setVerticalGroup(
            TopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LionStudyImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(TopPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(pennstatelogolabel, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(LionStudyText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        PsychTrackerTabs.setBackground(new java.awt.Color(0, 0, 0));

        ClientTab.setLayout(new java.awt.BorderLayout());

        clientPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 102), 1, true));

        clientComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientComboBoxActionPerformed(evt);
            }
        });

        clientLabel.setFont(new java.awt.Font("Sylfaen", 1, 18)); // NOI18N
        clientLabel.setText("Clients");

        addClientButton.setText("Add Client");
        addClientButton.setToolTipText("");
        addClientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClientButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Sort By:");

        SortButtonGroup.add(fnameradio);
        fnameradio.setSelected(true);
        fnameradio.setText("First Name");
        fnameradio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fnameradioActionPerformed(evt);
            }
        });

        SortButtonGroup.add(lanmeradio);
        lanmeradio.setText("Last Name");
        lanmeradio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lanmeradioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout clientPanelLayout = new javax.swing.GroupLayout(clientPanel);
        clientPanel.setLayout(clientPanelLayout);
        clientPanelLayout.setHorizontalGroup(
            clientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(clientPanelLayout.createSequentialGroup()
                        .addComponent(clientComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(addClientButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(fnameradio)
                        .addGap(18, 18, 18)
                        .addComponent(lanmeradio))
                    .addGroup(clientPanelLayout.createSequentialGroup()
                        .addComponent(clientLabel)
                        .addGap(492, 492, 492)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        clientPanelLayout.setVerticalGroup(
            clientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addClientButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnameradio)
                    .addComponent(lanmeradio))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        ClientTab.add(clientPanel, java.awt.BorderLayout.PAGE_START);

        namelabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel.setText("First Name:");

        namelabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel1.setText("Last Name:");

        namelabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel2.setText("Phone Number:");

        namelabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel3.setText("Email:");

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        fnamefield.setEditable(false);

        emailfield.setEditable(false);

        lnamefield.setEditable(false);
        lnamefield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lnamefieldActionPerformed(evt);
            }
        });

        phonefield.setEditable(false);

        donebuttonclient.setForeground(new java.awt.Color(255, 0, 0));
        donebuttonclient.setText("Done");
        donebuttonclient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                donebuttonclientActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FiguresPanelLayout = new javax.swing.GroupLayout(FiguresPanel);
        FiguresPanel.setLayout(FiguresPanelLayout);
        FiguresPanelLayout.setHorizontalGroup(
            FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FiguresPanelLayout.createSequentialGroup()
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FiguresPanelLayout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(namelabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(namelabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                .addComponent(namelabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(namelabel, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fnamefield, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(phonefield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                                .addComponent(emailfield, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lnamefield))))
                    .addGroup(FiguresPanelLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(editButton)
                        .addGap(37, 37, 37)
                        .addComponent(donebuttonclient)))
                .addContainerGap(716, Short.MAX_VALUE))
        );
        FiguresPanelLayout.setVerticalGroup(
            FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FiguresPanelLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namelabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnamefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namelabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lnamefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namelabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(emailfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namelabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phonefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(FiguresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButton)
                    .addComponent(donebuttonclient))
                .addContainerGap(95, Short.MAX_VALUE))
        );

        ClientTab.add(FiguresPanel, java.awt.BorderLayout.CENTER);

        PsychTrackerTabs.addTab("Client", new javax.swing.ImageIcon(getClass().getResource("/Res/ClientImg.png")), ClientTab); // NOI18N

        LoginButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        LoginButton.setText("Login");
        LoginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginButtonActionPerformed(evt);
            }
        });

        PasswordText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        PasswordText.setText("Password:");

        passwordfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordfieldKeyPressed(evt);
            }
        });

        UsernameText.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        UsernameText.setText("Username:");

        usernamefield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                usernamefieldKeyPressed(evt);
            }
        });

        LionStudyLoginText.setFont(new java.awt.Font("Verdana", 1, 28)); // NOI18N
        LionStudyLoginText.setText("PsychTracker Login");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(169, 169, 169)
                .addComponent(LionStudyLoginText)
                .addContainerGap(124, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(PasswordText)
                            .addComponent(UsernameText))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(usernamefield)
                            .addComponent(passwordfield, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(175, 175, 175))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(LoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(236, 236, 236))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(LionStudyLoginText)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernamefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UsernameText))
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PasswordText)
                    .addComponent(passwordfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addComponent(LoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout LoginTabLayout = new javax.swing.GroupLayout(LoginTab);
        LoginTab.setLayout(LoginTabLayout);
        LoginTabLayout.setHorizontalGroup(
            LoginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginTabLayout.createSequentialGroup()
                .addContainerGap(224, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(230, Short.MAX_VALUE))
        );
        LoginTabLayout.setVerticalGroup(
            LoginTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginTabLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        PsychTrackerTabs.addTab("Login", LoginTab);

        singuppasswordlabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        singuppasswordlabel.setText("Password:");

        signupusernamelabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        signupusernamelabel.setText("Username:");

        passwordfieldsignup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordfieldsignupKeyPressed(evt);
            }
        });

        usernamefieldsignup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernamefieldsignupActionPerformed(evt);
            }
        });
        usernamefieldsignup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                usernamefieldsignupKeyPressed(evt);
            }
        });

        SignUp.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        SignUp.setText("Sign Up");
        SignUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpActionPerformed(evt);
            }
        });

        reenterpasslabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        reenterpasslabel.setText("Re-Enter Password:");

        passwordfieldsignupreenter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordfieldsignupreenterKeyPressed(evt);
            }
        });

        LionStudySignUpLabel.setFont(new java.awt.Font("Verdana", 1, 28)); // NOI18N
        LionStudySignUpLabel.setText("PsychTracker Sign-Up");

        javax.swing.GroupLayout SignUpPanelLayout = new javax.swing.GroupLayout(SignUpPanel);
        SignUpPanel.setLayout(SignUpPanelLayout);
        SignUpPanelLayout.setHorizontalGroup(
            SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SignUpPanelLayout.createSequentialGroup()
                .addGap(0, 224, Short.MAX_VALUE)
                .addComponent(LionStudySignUpLabel)
                .addGap(216, 216, 216))
            .addGroup(SignUpPanelLayout.createSequentialGroup()
                .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SignUpPanelLayout.createSequentialGroup()
                        .addGap(210, 210, 210)
                        .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reenterpasslabel)
                            .addComponent(singuppasswordlabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(signupusernamelabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordfieldsignupreenter, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passwordfieldsignup, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernamefieldsignup, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(SignUpPanelLayout.createSequentialGroup()
                        .addGap(340, 340, 340)
                        .addComponent(SignUp, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SignUpPanelLayout.setVerticalGroup(
            SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SignUpPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(LionStudySignUpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernamefieldsignup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(signupusernamelabel))
                .addGap(27, 27, 27)
                .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singuppasswordlabel)
                    .addComponent(passwordfieldsignup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(SignUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reenterpasslabel)
                    .addComponent(passwordfieldsignupreenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(SignUp, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(175, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout SignUpTabLayout = new javax.swing.GroupLayout(SignUpTab);
        SignUpTab.setLayout(SignUpTabLayout);
        SignUpTabLayout.setHorizontalGroup(
            SignUpTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1056, Short.MAX_VALUE)
            .addGroup(SignUpTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(SignUpTabLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(SignUpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        SignUpTabLayout.setVerticalGroup(
            SignUpTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 518, Short.MAX_VALUE)
            .addGroup(SignUpTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(SignUpTabLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(SignUpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        PsychTrackerTabs.addTab("Sign Up", SignUpTab);

        figurelist.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "No Figures" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        figurelist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        figurelist.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                figurelistValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(figurelist);

        fnamefieldfigure.setEditable(false);

        namelabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel4.setText("First Name:");

        namelabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel5.setText("Last Name:");

        lnamefieldfigure.setEditable(false);
        lnamefieldfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lnamefieldfigureActionPerformed(evt);
            }
        });

        emailfieldfigure.setEditable(false);

        namelabel6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel6.setText("Email:");

        namelabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel7.setText("Phone Number:");

        phonefieldfigure.setEditable(false);

        editButtonfigure.setText("Edit");
        editButtonfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonfigureActionPerformed(evt);
            }
        });

        donebuttonclientfigure.setForeground(new java.awt.Color(255, 0, 0));
        donebuttonclientfigure.setText("Done");
        donebuttonclientfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                donebuttonclientfigureActionPerformed(evt);
            }
        });

        namelabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel8.setText("Rating:");

        ratingfieldfigure.setEditable(false);

        figurenotetextarea.setEditable(false);
        figurenotetextarea.setColumns(20);
        figurenotetextarea.setRows(5);
        jScrollPane3.setViewportView(figurenotetextarea);

        jLabel2.setText("Note:");

        namelabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        namelabel9.setText("Relationship:");

        relationshipfigurefield.setEditable(false);

        CreateFigureButton.setText("Create Figure");
        CreateFigureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateFigureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FiguresTabLayout = new javax.swing.GroupLayout(FiguresTab);
        FiguresTab.setLayout(FiguresTabLayout);
        FiguresTabLayout.setHorizontalGroup(
            FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FiguresTabLayout.createSequentialGroup()
                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FiguresTabLayout.createSequentialGroup()
                        .addGap(365, 365, 365)
                        .addComponent(editButtonfigure))
                    .addGroup(FiguresTabLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FiguresTabLayout.createSequentialGroup()
                                .addComponent(CreateFigureButton, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(FiguresTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(FiguresTabLayout.createSequentialGroup()
                                        .addGap(58, 58, 58)
                                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(namelabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(namelabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(namelabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(namelabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(13, 13, 13))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FiguresTabLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(namelabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(namelabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)))))))
                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(donebuttonclientfigure)
                    .addGroup(FiguresTabLayout.createSequentialGroup()
                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fnamefieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(phonefieldfigure, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                            .addComponent(emailfieldfigure)
                            .addComponent(ratingfieldfigure)
                            .addComponent(lnamefieldfigure, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(relationshipfigurefield))
                        .addGap(73, 73, 73)
                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        FiguresTabLayout.setVerticalGroup(
            FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FiguresTabLayout.createSequentialGroup()
                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FiguresTabLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FiguresTabLayout.createSequentialGroup()
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(namelabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fnamefieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(namelabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lnamefieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(namelabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(emailfieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(namelabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(phonefieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(ratingfieldfigure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(namelabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(relationshipfigurefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(namelabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(FiguresTabLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FiguresTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButtonfigure)
                    .addComponent(donebuttonclientfigure))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(CreateFigureButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );

        PsychTrackerTabs.addTab("Figures", FiguresTab);

        NoteTextArea.setEditable(false);
        NoteTextArea.setColumns(20);
        NoteTextArea.setRows(5);
        jScrollPane1.setViewportView(NoteTextArea);

        DateLabel.setText("00/00/00");

        EditNoteButton.setText("Edit");
        EditNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditNoteButtonActionPerformed(evt);
            }
        });

        CreateNoteButton.setText("Create");
        CreateNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateNoteButtonActionPerformed(evt);
            }
        });

        BackwardNoteButton.setText("<");
        BackwardNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackwardNoteButtonActionPerformed(evt);
            }
        });

        ForwardNoteButton.setText(">");
        ForwardNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ForwardNoteButtonActionPerformed(evt);
            }
        });

        DoneNoteButton.setBackground(new java.awt.Color(255, 51, 0));
        DoneNoteButton.setText("Done");
        DoneNoteButton.setSelected(true);
        DoneNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DoneNoteButtonActionPerformed(evt);
            }
        });

        DoneEditNoteButton.setBackground(new java.awt.Color(255, 51, 0));
        DoneEditNoteButton.setText("Done");
        DoneEditNoteButton.setSelected(true);
        DoneEditNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DoneEditNoteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout NotesTabLayout = new javax.swing.GroupLayout(NotesTab);
        NotesTab.setLayout(NotesTabLayout);
        NotesTabLayout.setHorizontalGroup(
            NotesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotesTabLayout.createSequentialGroup()
                .addContainerGap(134, Short.MAX_VALUE)
                .addComponent(BackwardNoteButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 692, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ForwardNoteButton)
                .addContainerGap(134, Short.MAX_VALUE))
            .addGroup(NotesTabLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(EditNoteButton)
                .addGap(18, 18, 18)
                .addComponent(DoneEditNoteButton)
                .addGap(171, 171, 171)
                .addComponent(DateLabel)
                .addGap(160, 160, 160)
                .addComponent(DoneNoteButton)
                .addGap(18, 18, 18)
                .addComponent(CreateNoteButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        NotesTabLayout.setVerticalGroup(
            NotesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotesTabLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(NotesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DateLabel)
                    .addComponent(EditNoteButton)
                    .addComponent(CreateNoteButton)
                    .addComponent(DoneNoteButton)
                    .addComponent(DoneEditNoteButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(NotesTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(NotesTabLayout.createSequentialGroup()
                        .addComponent(BackwardNoteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(ForwardNoteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        PsychTrackerTabs.addTab("Notes", NotesTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PsychTrackerTabs)
                    .addComponent(TopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TopPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PsychTrackerTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 528, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClientButtonActionPerformed
         ClientDialog cd = new ClientDialog(this,true);
         cd.setLocationRelativeTo(null);
         Client temp = cd.getGoing();
       if(temp != null){
            clients.add(temp);
            if(this.fnameradio.isSelected()){
                this.sortClientsbyFirstName(clients);
                this.refreshClientListFName();
            }
            else{
                this.sortClientsByLastName(clients);
                this.refreshClientListLName();
            }
       }
    }//GEN-LAST:event_addClientButtonActionPerformed

    private void clientComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientComboBoxActionPerformed
        index = this.clientComboBox.getSelectedIndex();
        this.LoadInAllClientData(clients.get(index));
    }//GEN-LAST:event_clientComboBoxActionPerformed

    private void usernamefieldsignupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernamefieldsignupActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernamefieldsignupActionPerformed

    private void SignUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SignUpActionPerformed
        if(this.usernamefieldsignup.getText().equals("") || this.passwordfieldsignup.getText().equals("") || this.passwordfieldsignupreenter.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Please fille out all fields.", "Error",JOptionPane.ERROR_MESSAGE);
        }
        else if(!this.passwordfieldsignup.getText().equals(this.passwordfieldsignupreenter.getText())){
            JOptionPane.showMessageDialog(null, "Passwords do not match.", "Error",JOptionPane.ERROR_MESSAGE);
        }
        else{
            username = this.usernamefieldsignup.getText().toLowerCase();
            ciph = this.passwordfieldsignup.getText();
            String path = System.getProperty("user.home") + File.separator + "Documents";
            path += File.separator + "PsychTracker";
            File customDir = new File(path);
            path += File.separator + username + ".dat";
            File file = new File(path);
            if (customDir.exists() && file.exists()) {
                    JOptionPane.showMessageDialog(null, "Looks like you already have an account!", "Error",JOptionPane.ERROR_MESSAGE);
            }
            else if(customDir.exists()){
                try {
                    this.EncryptFile();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                JOptionPane.showMessageDialog(null, "Created an account!", "Success",JOptionPane.INFORMATION_MESSAGE);
                try {
                    this.DecrpytFile();
                } catch (InvalidKeyException ex) {
                    Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.SetUpNormalUserScenario();
                this.refreshClientListFName();
            }
            else if (customDir.mkdirs()) {
                try {
                    this.EncryptFile();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                JOptionPane.showMessageDialog(null, "Created an account!", "Success",JOptionPane.INFORMATION_MESSAGE);
                this.SetUpNormalUserScenario();
                this.refreshClientListFName();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to use file system.", "Error",JOptionPane.ERROR_MESSAGE);

            }
        }
    }//GEN-LAST:event_SignUpActionPerformed

    private void lanmeradioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lanmeradioActionPerformed
        this.sortClientsByLastName(clients);
        this.refreshClientListLName();
    }//GEN-LAST:event_lanmeradioActionPerformed

    private void fnameradioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fnameradioActionPerformed
       this.sortClientsbyFirstName(clients);
       this.refreshClientListFName();
    }//GEN-LAST:event_fnameradioActionPerformed

    private void LoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoginButtonActionPerformed
        if(this.usernamefield.getText().equals("") || this.passwordfield.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Please fill out all fields first.", "Error",JOptionPane.ERROR_MESSAGE);
        }
        else{
            username = this.usernamefield.getText().toLowerCase();
            ciph = this.passwordfield.getText();
            String path = System.getProperty("user.home") + File.separator + "Documents";
            path += File.separator + "PsychTracker" + File.separator + this.username + ".dat";
            File f = new File(path);
            if(f.exists()){
            try {
                this.DecrpytFile();
            } catch (InvalidKeyException ex) {
                Logger.getLogger(PsychTrackerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            else{
                JOptionPane.showMessageDialog(null, "Could not find that username. Did you sign up?", "Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_LoginButtonActionPerformed

    private void CreateNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateNoteButtonActionPerformed
        if(index != -1){
        this.NoteTextArea.setText("Edit me!");
        this.NoteTextArea.setEditable(true);
        this.DoneNoteButton.setVisible(true);
        this.CreateNoteButton.setEnabled(false);
        }
        else{
            JOptionPane.showMessageDialog(null, "Please select a client first.", "Error",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_CreateNoteButtonActionPerformed

    private void DoneNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DoneNoteButtonActionPerformed
        clients.get(index).AddNote(this.NoteTextArea.getText(), new Date());
        this.currentNote = clients.size()-1;
        this.NoteTextArea.setEditable(false);
        this.DoneNoteButton.setVisible(false);
        this.currentNote = clients.get(index).getNotes().size()-1;
        this.refreshNotes(clients.get(index));
        this.CreateNoteButton.setEnabled(true);
        this.EditNoteButton.setEnabled(true);
        JOptionPane.showMessageDialog(null, "Your note has been successfully saved.", "Success",JOptionPane.DEFAULT_OPTION);
    }//GEN-LAST:event_DoneNoteButtonActionPerformed

    private void BackwardNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardNoteButtonActionPerformed
        if (currentNote != 0) {
            this.NoteTextArea.setText(this.clients.get(index).getNotes().get(currentNote-1).getNote());
            int size = clients.get(index).getNotes().size();
            this.DateLabel.setText(this.clients.get(index).getNotes().get(currentNote-1).getDate().getMonth()+1 +"/"+ this.clients.get(index).getNotes().get(currentNote-1).getDate().getDate()+"/"+this.clients.get(index).getNotes().get(currentNote-1).getDate().getYear());
            currentNote--;
        }
        else{
            JOptionPane.showMessageDialog(null, "There are no more notes to go back to.", "Woops!",JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_BackwardNoteButtonActionPerformed

    private void ForwardNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForwardNoteButtonActionPerformed
        if (currentNote != clients.get(index).getNotes().size()-1) {
            this.NoteTextArea.setText(this.clients.get(index).getNotes().get(currentNote+1).getNote());
            int size = clients.get(index).getNotes().size();
            this.DateLabel.setText(this.clients.get(index).getNotes().get(currentNote+1).getDate().getMonth()+1 +"/"+ this.clients.get(index).getNotes().get(currentNote+1).getDate().getDate()+"/"+this.clients.get(index).getNotes().get(currentNote+1).getDate().getYear());
            currentNote++;
        }
        else{
            JOptionPane.showMessageDialog(null, "There are no more notes.", "Woops!",JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_ForwardNoteButtonActionPerformed

    private void usernamefieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernamefieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.LoginButton.doClick();
        }
    }//GEN-LAST:event_usernamefieldKeyPressed

    private void passwordfieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordfieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.LoginButton.doClick();
        }
    }//GEN-LAST:event_passwordfieldKeyPressed

    private void usernamefieldsignupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernamefieldsignupKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.SignUp.doClick();
        }
    }//GEN-LAST:event_usernamefieldsignupKeyPressed

    private void passwordfieldsignupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordfieldsignupKeyPressed
       if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.SignUp.doClick();
        }
    }//GEN-LAST:event_passwordfieldsignupKeyPressed

    private void passwordfieldsignupreenterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordfieldsignupreenterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.SignUp.doClick();
        }
    }//GEN-LAST:event_passwordfieldsignupreenterKeyPressed

    private void lnamefieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lnamefieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lnamefieldActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        this.emailfield.setEditable(true);
        this.lnamefield.setEditable(true);
        this.fnamefield.setEditable(true);
        this.phonefield.setEditable(true);
        this.donebuttonclient.setVisible(true);
        this.editButton.setEnabled(false);
    }//GEN-LAST:event_editButtonActionPerformed

    private void donebuttonclientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_donebuttonclientActionPerformed
        if (this.fnamefield.getText().equals("") || this.lnamefield.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "First and Last Name cannot be blank", "Woops!",JOptionPane.DEFAULT_OPTION);
        } else {
            this.emailfield.setEditable(false);
            this.lnamefield.setEditable(false);
            this.fnamefield.setEditable(false);
            this.phonefield.setEditable(false);
            this.donebuttonclient.setVisible(false);
            this.editButton.setEnabled(true);
            clients.get(index).setEmail(emailfield.getText());
            clients.get(index).setFirstname(fnamefield.getText());
            clients.get(index).setLastname(lnamefield.getText());
            if(!this.phonefield.getText().equals("")){
                try{
            clients.get(index).setPhonenumber(new BigInteger((phonefield.getText())));
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(null, "Phone number is not valid, saved everything but phone number.", "Woops!",JOptionPane.DEFAULT_OPTION);
            }
            }
            
            this.FillinClientTab();
        }
    }//GEN-LAST:event_donebuttonclientActionPerformed

    private void EditNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditNoteButtonActionPerformed
        this.CreateNoteButton.setEnabled(false);
        this.NoteTextArea.setEditable(true);
        this.EditNoteButton.setEnabled(false);
        this.DoneEditNoteButton.setVisible(true);
 
    }//GEN-LAST:event_EditNoteButtonActionPerformed

    private void DoneEditNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DoneEditNoteButtonActionPerformed
        if(this.NoteTextArea.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Note cannot be blank!", "Error",JOptionPane.ERROR_MESSAGE);
        }
        else{
        clients.get(index).getNotes().get(currentNote).setNote(this.NoteTextArea.getText());
        this.NoteTextArea.setEditable(false);
        this.DoneEditNoteButton.setVisible(false);
        this.EditNoteButton.setEnabled(true);
        this.CreateNoteButton.setEnabled(true);
        this.NoteTextArea.setText(this.clients.get(index).getNotes().get(currentNote).getNote());
        JOptionPane.showMessageDialog(null, "Your note has been successfully saved.", "Success",JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_DoneEditNoteButtonActionPerformed

    private void lnamefieldfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lnamefieldfigureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lnamefieldfigureActionPerformed

    private void editButtonfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonfigureActionPerformed
        if(selectedFigure!= -1){
        this.emailfieldfigure.setEditable(true);
        this.lnamefieldfigure.setEditable(true);
        this.ratingfieldfigure.setEditable(true);
        this.fnamefieldfigure.setEditable(true);
        this.phonefieldfigure.setEditable(true);
        this.relationshipfigurefield.setEditable(true);
        this.donebuttonclientfigure.setVisible(true);
        this.figurenotetextarea.setEditable(true);
        this.editButtonfigure.setEnabled(false);
        }
        else{
            JOptionPane.showMessageDialog(null, "You did not select a figure.", "Woops!", JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_editButtonfigureActionPerformed

    private void donebuttonclientfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_donebuttonclientfigureActionPerformed
        if (this.fnamefieldfigure.getText().equals("") || this.lnamefieldfigure.getText().equals("") || this.relationshipfigurefield.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "First and Last Name and Relationship cannot be blank", "Woops!", JOptionPane.DEFAULT_OPTION);
        } else {
            this.emailfieldfigure.setEditable(false);
            this.lnamefieldfigure.setEditable(false);
            this.fnamefieldfigure.setEditable(false);
            this.phonefieldfigure.setEditable(false);
            this.donebuttonclientfigure.setVisible(false);
            this.editButtonfigure.setEnabled(true);
            this.ratingfieldfigure.setEditable(false);
            this.figurenotetextarea.setEditable(false);
            this.relationshipfigurefield.setEditable(false);
            clients.get(index).getFigures().get(selectedFigure).setEmail(emailfieldfigure.getText());
            clients.get(index).getFigures().get(selectedFigure).setFirstname(fnamefieldfigure.getText());
            clients.get(index).getFigures().get(selectedFigure).setLastname(lnamefieldfigure.getText());
            clients.get(index).getFigures().get(selectedFigure).setRelationship(this.relationshipfigurefield.getText());
            clients.get(index).getFigures().get(selectedFigure).setNote(this.figurenotetextarea.getText());
            if(!this.phonefieldfigure.getText().equals("")){
              try {
                clients.get(index).getFigures().get(index).setPhonenumber(new BigInteger(phonefieldfigure.getText()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Phone number is not valid, saved everything but phone number.", "Woops!", JOptionPane.DEFAULT_OPTION);
                this.phonefieldfigure.setText("");
            }  
            }
            if(!this.ratingfieldfigure.getText().equals("")){
            try {
                clients.get(index).getFigures().get(selectedFigure).setRating((int)Long.parseLong(ratingfieldfigure.getText()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Rating is not valid, saved everything but rating.", "Woops!", JOptionPane.DEFAULT_OPTION);
                this.ratingfieldfigure.setText("");
            }
            }
            this.FillinClientTab();
        }
    }//GEN-LAST:event_donebuttonclientfigureActionPerformed

    private void figurelistValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_figurelistValueChanged
        if(clients.get(index).getFigures().size()>0){
         selectedFigure = this.figurelist.getSelectedIndex();
        this.FillinFigureTab();
        }
    }//GEN-LAST:event_figurelistValueChanged

    private void CreateFigureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateFigureButtonActionPerformed
         FigureDialog fd = new FigureDialog(this,true);
         fd.setLocationRelativeTo(null);
         Figures temp = fd.getGoing();
       if(temp != null){
           clients.get(index).getFigures().add(temp);
           this.refreshFigureList();
       }
    }//GEN-LAST:event_CreateFigureButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PsychTrackerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PsychTrackerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PsychTrackerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PsychTrackerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PsychTrackerGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackwardNoteButton;
    private javax.swing.JPanel ClientTab;
    private javax.swing.JButton CreateFigureButton;
    private javax.swing.JButton CreateNoteButton;
    private javax.swing.JLabel DateLabel;
    private javax.swing.JButton DoneEditNoteButton;
    private javax.swing.JButton DoneNoteButton;
    private javax.swing.JButton EditNoteButton;
    private javax.swing.JPanel FiguresPanel;
    private javax.swing.JPanel FiguresTab;
    private javax.swing.JButton ForwardNoteButton;
    private javax.swing.JLabel LionStudyImage;
    private javax.swing.JLabel LionStudyLoginText;
    private javax.swing.JLabel LionStudySignUpLabel;
    private javax.swing.JLabel LionStudyText;
    private javax.swing.JButton LoginButton;
    private javax.swing.JPanel LoginTab;
    private javax.swing.JTextArea NoteTextArea;
    private javax.swing.JPanel NotesTab;
    private javax.swing.JLabel PasswordText;
    private javax.swing.JTabbedPane PsychTrackerTabs;
    private javax.swing.JButton SignUp;
    private javax.swing.JPanel SignUpPanel;
    private javax.swing.JPanel SignUpTab;
    private javax.swing.ButtonGroup SortButtonGroup;
    private javax.swing.JPanel TopPanel;
    private javax.swing.JLabel UsernameText;
    private javax.swing.JButton addClientButton;
    private javax.swing.JComboBox<String> clientComboBox;
    private javax.swing.JLabel clientLabel;
    private javax.swing.JPanel clientPanel;
    private javax.swing.JButton donebuttonclient;
    private javax.swing.JButton donebuttonclientfigure;
    private javax.swing.JButton editButton;
    private javax.swing.JButton editButtonfigure;
    private javax.swing.JTextField emailfield;
    private javax.swing.JTextField emailfieldfigure;
    private javax.swing.JList figurelist;
    private javax.swing.JTextArea figurenotetextarea;
    private javax.swing.JTextField fnamefield;
    private javax.swing.JTextField fnamefieldfigure;
    private javax.swing.JRadioButton fnameradio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JRadioButton lanmeradio;
    private javax.swing.JTextField lnamefield;
    private javax.swing.JTextField lnamefieldfigure;
    private javax.swing.JLabel namelabel;
    private javax.swing.JLabel namelabel1;
    private javax.swing.JLabel namelabel2;
    private javax.swing.JLabel namelabel3;
    private javax.swing.JLabel namelabel4;
    private javax.swing.JLabel namelabel5;
    private javax.swing.JLabel namelabel6;
    private javax.swing.JLabel namelabel7;
    private javax.swing.JLabel namelabel8;
    private javax.swing.JLabel namelabel9;
    private javax.swing.JPasswordField passwordfield;
    private javax.swing.JPasswordField passwordfieldsignup;
    private javax.swing.JPasswordField passwordfieldsignupreenter;
    private javax.swing.JLabel pennstatelogolabel;
    private javax.swing.JTextField phonefield;
    private javax.swing.JTextField phonefieldfigure;
    private javax.swing.JTextField ratingfieldfigure;
    private javax.swing.JLabel reenterpasslabel;
    private javax.swing.JTextField relationshipfigurefield;
    private javax.swing.JLabel signupusernamelabel;
    private javax.swing.JLabel singuppasswordlabel;
    private javax.swing.JTextField usernamefield;
    private javax.swing.JTextField usernamefieldsignup;
    // End of variables declaration//GEN-END:variables
}
