package jTextLite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class jTextLite extends MIDlet implements CommandListener, Runnable {
  Thread thread;
  
  Alert alert;
  
  Display display;
  
  RecordStore rs;
  
  Form f;
  
  Form f1;
  
  Form f2;
  
  Form f3;
  
  Form f4;
  
  Form f5;
  
  Form f6;
  
  Form f7;
  
  Form f8;
  
  Form f9;
  
  ChoiceGroup cgrf;
  
  ChoiceGroup cgReplaceAll;
  
  ChoiceGroup cgDelete;
  
  TextBox tb;
  
  TextBox helpText;
  
  TextField tf;
  
  TextField tf1;
  
  String password = "";
  
  boolean start = true;
  
  boolean checkPasswordAgain = false;
  
  boolean saveMode = false;
  
  boolean deletePlain = false;
  
  Command exitCommand = new Command("Exit", 7, 1);
  
  Command clearCommand = new Command("Clear", 2, 3);
  
  Command okCommand = new Command("OK", 2, 3);
  
  Command browseCommand = new Command("Browse", 2, 2);
  
  Command saveCommand = new Command("Save", 2, 3);
  
  Command newCommand = new Command("New", 2, 3);
  
  Command passwordCommand = new Command("Password", 2, 4);
  
  Command saveAsCommand = new Command("Save As", 2, 3);
  
  Command selectCommand = new Command("Select", 2, 3);
  
  Command recentCommand = new Command("Recent", 2, 3);
  
  Command findCommand = new Command("Find", 2, 3);
  
  Command findAgainCommand = new Command("Find Again", 2, 3);
  
  Command replaceCommand = new Command("Replace", 2, 3);
  
  Command goToCommand = new Command("Go To", 2, 3);
  
  Command topCommand = new Command("Top", 2, 3);
  
  Command bottomCommand = new Command("Bottom", 2, 3);
  
  Command cancelCommand = new Command("Cancel", 1, 3);
  
  Command viewCommand = new Command("View", 8, 1);
  
  Command backCommand = new Command("Back", 2, 2);
  
  Command preferencesCommand = new Command("Preferences", 2, 4);
  
  Command helpCommand = new Command("Help", 2, 4);
  
  Command aboutCommand = new Command("About", 2, 5);
  
  static final String SEED = "jCry";
  
  static final String UP_DIRECTORY = "..";
  
  static final String MEGA_ROOT = "/";
  
  static final String SEP_STR = "/";
  
  static final char SEP = '/';
  
  String currDirName = "";
  
  String saveFile = "";
  
  String[] recentFiles;
  
  Image dirIcon;
  
  Image fileIcon;
  
  Image[] iconList;
  
  int[] state;
  
  int x;
  
  int y;
  
  int temp;
  
  int runMode;
  
  int passwordError = 0;
  
  int largeFileWarning = 20000;
  
  int caretPosition = 0;
  
  public jTextLite() {
    this.currDirName = "/";
    try {
      this.dirIcon = Image.createImage("/icons/dir.png");
    } catch (IOException iOException) {
      this.dirIcon = null;
    } 
    try {
      this.fileIcon = Image.createImage("/icons/file.png");
    } catch (IOException iOException) {
      this.fileIcon = null;
    } 
    this.iconList = new Image[] { this.fileIcon, this.dirIcon };
    this.recentFiles = new String[4];
    for (byte b = 0; b < 4; b++)
      this.recentFiles[b] = "(empty)"; 
  }
  
  public void startApp() throws MIDletStateChangeException {
    if (this.start) {
      this.start = false;
      openDataStore();
      this.display = Display.getDisplay(this);
      this.alert = new Alert("User Information");
      this.alert.setType(AlertType.CONFIRMATION);
      this.alert.setTimeout(1500);
      this.tb = new TextBox("Text Editor Lite", null, 20000, 0);
      getData();
      this.tb.addCommand(this.exitCommand);
      this.tb.addCommand(this.saveCommand);
      this.tb.addCommand(this.saveAsCommand);
      this.tb.addCommand(this.recentCommand);
      this.tb.addCommand(this.passwordCommand);
      this.tb.addCommand(this.clearCommand);
      this.tb.addCommand(this.browseCommand);
      this.tb.addCommand(this.findCommand);
      this.tb.addCommand(this.findAgainCommand);
      this.tb.addCommand(this.replaceCommand);
      this.tb.addCommand(this.goToCommand);
      this.tb.addCommand(this.topCommand);
      this.tb.addCommand(this.bottomCommand);
      this.tb.addCommand(this.preferencesCommand);
      this.tb.addCommand(this.helpCommand);
      this.tb.addCommand(this.aboutCommand);
      this.tb.setCommandListener(this);
      this.display.setCurrent((Displayable)this.tb);
      this.runMode = 7;
      runThread();
    } 
  }
  
  void runThread() {
    this.thread = new Thread(this);
    this.thread.start();
  }
  
  public void pauseApp() {}
  
  public void destroyApp(boolean paramBoolean) {
    notifyDestroyed();
  }
  
  public void run() {
    if (this.runMode == 1)
      saveText(); 
    if (this.runMode == 2)
      showRecentFiles(); 
    if (this.runMode == 3)
      showCurrDir(); 
    if (this.runMode == 4)
      showFile(this.saveFile); 
    if (this.runMode == 5)
      traverseDirectory(this.saveFile); 
    if (this.runMode == 6)
      processHelp(); 
    if (this.runMode == 7)
      setCaretPosition(); 
  }
  
  void setCaretPositionToTop() {
    this.caretPosition = 0;
    this.runMode = 7;
    runThread();
  }
  
  void setCaretPosition() {
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException interruptedException) {}
    this.tb.insert("", this.caretPosition);
  }
  
  void showCurrDir() {
    FileConnection fileConnection = null;
    try {
      Enumeration enumeration;
      List list;
      if ("/".equals(this.currDirName)) {
        enumeration = FileSystemRegistry.listRoots();
        list = new List(this.currDirName, 3);
      } else {
        fileConnection = (FileConnection)Connector.open("file://localhost/" + this.currDirName, 1);
        enumeration = fileConnection.list();
        list = new List(this.currDirName, 3);
        list.append("..", this.dirIcon);
      } 
      while (enumeration.hasMoreElements()) {
        String str = enumeration.nextElement();
        if (str.charAt(str.length() - 1) == '/') {
          list.append(str, this.dirIcon);
          continue;
        } 
        list.append(str, this.fileIcon);
      } 
      if (this.saveMode) {
        list.setSelectCommand(this.selectCommand);
        list.addCommand(this.newCommand);
      } else {
        list.setSelectCommand(this.viewCommand);
      } 
      list.addCommand(this.backCommand);
      list.addCommand(this.exitCommand);
      list.setCommandListener(this);
      if (fileConnection != null)
        fileConnection.close(); 
      this.display.setCurrent((Displayable)list);
    } catch (IOException iOException) {
      iOException.printStackTrace();
    } 
  }
  
  void traverseDirectory(String paramString) {
    if (this.currDirName.equals("/")) {
      if (paramString.equals(".."))
        return; 
      this.currDirName = paramString;
    } else if (paramString.equals("..")) {
      int i = this.currDirName.lastIndexOf('/', this.currDirName.length() - 2);
      if (i != -1) {
        this.currDirName = this.currDirName.substring(0, i + 1);
      } else {
        this.currDirName = "/";
      } 
    } else {
      this.currDirName += paramString;
    } 
    showCurrDir();
  }
  
  void showFile(String paramString) {
    try {
      FileConnection fileConnection = (FileConnection)Connector.open("file://localhost/" + this.currDirName + paramString, 1);
      if (!fileConnection.exists())
        throw new IOException("File does not exist"); 
      int i = (int)fileConnection.fileSize();
      if (this.largeFileWarning > 0 && i > this.largeFileWarning) {
        this.alert.setString("File exceeds large file limit");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.tb.setMaxSize(i + 5000);
      DataInputStream dataInputStream = fileConnection.openDataInputStream();
      byte[] arrayOfByte = new byte[i];
      int j = dataInputStream.read(arrayOfByte, 0, i);
      dataInputStream.close();
      fileConnection.close();
      if (j == 0) {
        this.display.setCurrent((Displayable)this.tb);
        return;
      } 
      if (paramString.endsWith("#") || paramString.endsWith("#.rem")) {
        initialise();
        setKey(this.password);
        int k = "jCry".length();
        byte[] arrayOfByte1 = new byte[k];
        for (byte b1 = 0; b1 < "jCry".length(); b1++)
          arrayOfByte1[b1] = RC4(arrayOfByte[b1]); 
        String str = new String(arrayOfByte1);
        if (!str.equals("jCry")) {
          this.alert.setString("Password error");
          this.display.setCurrent(this.alert, (Displayable)this.tb);
          return;
        } 
        for (byte b2 = 0; b2 < i - k; b2++)
          arrayOfByte[b2] = RC4(arrayOfByte[b2 + k]); 
        try {
          this.tb.setString(new String(arrayOfByte, 0, j - k, "UTF8"));
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
          System.out.println("UTF8 not supported");
          this.tb.setString(new String(arrayOfByte, 0, j - k));
        } 
      } else {
        try {
          this.tb.setString(new String(arrayOfByte, 0, j, "UTF8"));
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
          System.out.println("UTF8 not supported");
          this.tb.setString(new String(arrayOfByte, 0, j));
        } 
      } 
      updateRecentFiles();
      this.display.setCurrent((Displayable)this.tb);
      setCaretPositionToTop();
    } catch (Exception exception) {
      Alert alert = new Alert("User Information", "Can not access file " + paramString + " in directory " + this.currDirName + "\nException: " + exception.getMessage(), null, AlertType.ERROR);
      alert.setTimeout(-2);
      this.display.setCurrent((Displayable)alert);
    } 
  }
  
  void saveText() {
    try {
      byte[] arrayOfByte;
      if (this.saveFile.equals("")) {
        this.saveMode = true;
        this.runMode = 3;
        runThread();
        return;
      } 
      FileConnection fileConnection = (FileConnection)Connector.open("file://localhost/" + this.currDirName + this.saveFile);
      if (fileConnection.exists())
        fileConnection.delete(); 
      fileConnection.create();
      DataOutputStream dataOutputStream = fileConnection.openDataOutputStream();
      try {
        arrayOfByte = this.tb.getString().getBytes("UTF8");
      } catch (UnsupportedEncodingException unsupportedEncodingException) {
        System.out.println("UTF8 not supported");
        arrayOfByte = this.tb.getString().getBytes();
      } 
      if (this.saveFile.endsWith("#") || this.saveFile.endsWith("#.rem")) {
        initialise();
        setKey(this.password);
        byte[] arrayOfByte1 = "jCry".getBytes();
        byte b;
        for (b = 0; b < arrayOfByte1.length; b++)
          arrayOfByte1[b] = RC4(arrayOfByte1[b]); 
        dataOutputStream.write(arrayOfByte1);
        for (b = 0; b < arrayOfByte.length; b++)
          arrayOfByte[b] = RC4(arrayOfByte[b]); 
      } 
      dataOutputStream.write(arrayOfByte);
      dataOutputStream.close();
      if (this.saveFile.endsWith("#") || this.saveFile.endsWith("#.rem")) {
        String str = "";
        if (this.saveFile.endsWith("#"))
          str = this.saveFile.substring(0, this.saveFile.length() - 1); 
        if (this.saveFile.endsWith("#.rem"))
          str = this.saveFile.substring(0, this.saveFile.length() - 4); 
        if (this.deletePlain) {
          FileConnection fileConnection1 = (FileConnection)Connector.open("file://localhost/" + this.currDirName + str);
          if (fileConnection1.exists())
            fileConnection1.delete(); 
          fileConnection1.close();
        } 
      } 
      fileConnection.close();
      updateRecentFiles();
      this.alert.setString("Saved");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
    } catch (Exception exception) {
      Alert alert = new Alert("Error!", "Can not access file " + this.saveFile + " in directory " + this.currDirName + "\nException: " + exception.getMessage(), null, AlertType.ERROR);
      alert.setTimeout(-2);
      this.display.setCurrent((Displayable)alert);
    } 
  }
  
  void updateRecentFiles() {
    if (this.recentFiles[0].equals(this.currDirName + this.saveFile))
      return; 
    if (this.recentFiles[1].equals(this.currDirName + this.saveFile)) {
      this.recentFiles[1] = this.recentFiles[0];
      this.recentFiles[0] = this.currDirName + this.saveFile;
      return;
    } 
    if (this.recentFiles[2].equals(this.currDirName + this.saveFile)) {
      this.recentFiles[2] = this.recentFiles[1];
      this.recentFiles[1] = this.recentFiles[0];
      this.recentFiles[0] = this.currDirName + this.saveFile;
      return;
    } 
    this.recentFiles[3] = this.recentFiles[2];
    this.recentFiles[2] = this.recentFiles[1];
    this.recentFiles[1] = this.recentFiles[0];
    this.recentFiles[0] = this.currDirName + this.saveFile;
  }
  
  void setKey(String paramString) {
    byte[] arrayOfByte = paramString.getBytes();
    int i;
    for (i = 0; i < 256; i++)
      this.state[i] = i; 
    i = 0;
    int j = 0;
    byte b = 0;
    do {
      j = (arrayOfByte[i] + this.state[b] + j) % 256;
      this.temp = this.state[b];
      this.state[b] = this.state[j];
      this.state[j] = this.temp;
      i = (i + 1) % paramString.length();
    } while (++b < ');
  }
  
  byte RC4(byte paramByte) {
    this.x %= 256;
    this.y = (this.state[this.x] + this.y) % 256;
    this.temp = this.state[this.x];
    this.state[this.x] = this.state[this.y];
    this.state[this.y] = this.temp;
    this.temp = (this.state[this.x] + this.temp) % 256;
    return (byte)(paramByte ^ this.state[this.temp]);
  }
  
  void initialise() {
    this.x = 0;
    this.y = 0;
    this.state = new int[256];
  }
  
  void showRecentFiles() {
    this.f3 = new Form("Recent Files");
    this.cgrf = new ChoiceGroup("Choose recent file", 1);
    for (byte b = 0; b < 4; b++)
      this.cgrf.append(this.recentFiles[b], null); 
    this.f3.append((Item)this.cgrf);
    this.f3.addCommand(this.okCommand);
    this.f3.addCommand(this.cancelCommand);
    this.f3.setCommandListener(this);
    this.display.setCurrent((Displayable)this.f3);
  }
  
  void getPassword() {
    this.f = new Form("Check Password");
    this.f.append("Type your password");
    this.tf = new TextField("Password", "", 20, 65536);
    this.f.append((Item)this.tf);
    this.f.addCommand(this.okCommand);
    this.f.setCommandListener(this);
    this.display.setCurrent((Displayable)this.f);
  }
  
  void setPassword() {
    this.f1 = new Form("New Password");
    this.f1.append("Type your password");
    this.tf = new TextField("Password", "", 20, 65536);
    this.f1.append((Item)this.tf);
    this.tf1 = new TextField("Again", "", 20, 65536);
    this.f1.append((Item)this.tf1);
    this.f1.append("Delete plain text file after encryption?");
    this.cgDelete = new ChoiceGroup("Choose", 1);
    this.cgDelete.append("Delete", null);
    this.cgDelete.append("Keep", null);
    this.f1.append((Item)this.cgDelete);
    this.f1.addCommand(this.okCommand);
    this.f1.setCommandListener(this);
    this.display.setCurrent((Displayable)this.f1);
  }
  
  void openDataStore() {
    try {
      this.rs = RecordStore.openRecordStore("jTextLite", true, 1, true);
    } catch (RecordStoreException recordStoreException) {
      System.err.println(recordStoreException + " te1");
    } 
  }
  
  void closeDataStore() {
    if (this.rs != null)
      try {
        this.rs.closeRecordStore();
      } catch (RecordStoreException recordStoreException) {
        System.err.println(recordStoreException + " te2");
      }  
  }
  
  int getNumRecords() {
    if (this.rs == null)
      return 0; 
    try {
      return this.rs.getNumRecords();
    } catch (RecordStoreException recordStoreException) {
      System.err.println(recordStoreException + " te3");
      return 0;
    } 
  }
  
  byte[] toByteArray() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    try {
      dataOutputStream.writeUTF(this.tb.getString());
      dataOutputStream.writeInt(this.tb.getCaretPosition());
      for (byte b = 0; b < 4; b++)
        dataOutputStream.writeUTF(this.recentFiles[b]); 
      dataOutputStream.writeInt(this.largeFileWarning);
    } catch (IOException iOException) {}
    return byteArrayOutputStream.toByteArray();
  }
  
  void fromByteArray(byte[] paramArrayOfbyte) {
    try {
      DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(paramArrayOfbyte));
      this.tb.setString(dataInputStream.readUTF());
      this.caretPosition = dataInputStream.readInt();
      for (byte b = 0; b < 4; b++)
        this.recentFiles[b] = dataInputStream.readUTF(); 
      this.largeFileWarning = dataInputStream.readInt();
    } catch (IOException iOException) {
      System.err.println(iOException + " te4a");
    } 
  }
  
  void getData() {
    try {
      if (this.rs.getNumRecords() == 0)
        return; 
      byte[] arrayOfByte = this.rs.getRecord(1);
      fromByteArray(arrayOfByte);
    } catch (RecordStoreException recordStoreException) {
      System.err.println(recordStoreException + " te4");
    } 
  }
  
  void saveData() {
    if (this.rs != null)
      try {
        if (this.saveFile.endsWith("#") || this.saveFile.endsWith("#.rem"))
          this.tb.setString(""); 
        byte[] arrayOfByte = toByteArray();
        if (this.rs.getNumRecords() > 0) {
          this.rs.setRecord(1, arrayOfByte, 0, arrayOfByte.length);
        } else {
          this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
        } 
      } catch (RecordStoreException recordStoreException) {
        System.err.println(recordStoreException + " te7");
      }  
  }
  
  int getSize() {
    try {
      return this.rs.getSize();
    } catch (RecordStoreException recordStoreException) {
      System.err.println(recordStoreException + " te9");
      return 0;
    } 
  }
  
  void processHelp() {
    char c = ';
    this.helpText = new TextBox("jTextLite Help", null, c, 131072);
    this.helpText.addCommand(this.okCommand);
    this.helpText.addCommand(this.exitCommand);
    this.helpText.setCommandListener(this);
    try {
      DataInputStream dataInputStream = new DataInputStream(getClass().getResourceAsStream("/Text/jTextLiteHelp.txt"));
      if (dataInputStream == null) {
        this.helpText.setString("Could not open help text");
      } else {
        byte[] arrayOfByte = new byte[c];
        int i = dataInputStream.read(arrayOfByte, 0, c);
        this.helpText.setString(new String(arrayOfByte, 0, i));
        dataInputStream.close();
      } 
    } catch (IOException iOException) {
      this.helpText.setString("Failed to load text");
    } 
    this.display.setCurrent((Displayable)this.helpText);
  }
  
  public void commandAction(Command paramCommand, Displayable paramDisplayable) {
    if (paramCommand == this.exitCommand) {
      saveData();
      closeDataStore();
      destroyApp(true);
      return;
    } 
    if (paramCommand == this.clearCommand) {
      this.f2 = new Form("Clear all text");
      this.f2.append("Do you want to clear all text?");
      this.f2.addCommand(this.okCommand);
      this.f2.addCommand(this.cancelCommand);
      this.f2.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f2);
      return;
    } 
    if (paramCommand == this.browseCommand) {
      this.saveMode = false;
      this.runMode = 3;
      runThread();
      return;
    } 
    if (paramCommand == this.saveAsCommand) {
      this.saveMode = true;
      this.runMode = 3;
      runThread();
      return;
    } 
    if (paramCommand == this.saveCommand) {
      this.runMode = 1;
      runThread();
      return;
    } 
    if (paramCommand == this.viewCommand) {
      List list = (List)paramDisplayable;
      String str = list.getString(list.getSelectedIndex());
      if (this.saveFile.equals(str))
        return; 
      this.saveFile = str;
      if (str.endsWith("/") || str.equals("..")) {
        this.runMode = 5;
        runThread();
      } else if (this.saveFile.endsWith("#") || this.saveFile.endsWith("#.rem")) {
        getPassword();
      } else {
        this.runMode = 4;
        runThread();
      } 
      return;
    } 
    if (paramCommand == this.selectCommand) {
      List list = (List)paramDisplayable;
      String str = list.getString(list.getSelectedIndex());
      if (this.saveFile.equals(str))
        return; 
      this.saveFile = str;
      if (str.endsWith("/") || str.equals("..")) {
        this.runMode = 5;
        runThread();
      } else {
        this.runMode = 1;
        runThread();
      } 
      return;
    } 
    if (paramCommand == this.newCommand) {
      this.f4 = new Form("Save as new file");
      this.tf = new TextField("Enter name", null, 256, 0);
      this.f4.append((Item)this.tf);
      this.f4.addCommand(this.okCommand);
      this.f4.addCommand(this.cancelCommand);
      this.f4.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f4);
    } 
    if (paramCommand == this.recentCommand) {
      this.runMode = 2;
      runThread();
      return;
    } 
    if (paramCommand == this.findCommand) {
      this.f7 = new Form("Find string");
      this.tf = new TextField("String", null, 32, 0);
      this.f7.append((Item)this.tf);
      this.f7.addCommand(this.okCommand);
      this.f7.addCommand(this.cancelCommand);
      this.f7.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f7);
    } 
    if (paramCommand == this.findAgainCommand) {
      int i = this.tb.getString().toLowerCase().substring(this.caretPosition + 1).indexOf(this.tf.getString().toLowerCase());
      if (i < 0) {
        this.alert.setString("String not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.caretPosition += 1 + i;
      this.display.setCurrent((Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.replaceCommand) {
      this.f8 = new Form("Replace");
      this.tf = new TextField("Old String", null, 32, 0);
      this.f8.append((Item)this.tf);
      this.tf1 = new TextField("New String", null, 32, 0);
      this.f8.append((Item)this.tf1);
      this.cgReplaceAll = new ChoiceGroup("Replace All", 1);
      this.cgReplaceAll.append("No", null);
      this.cgReplaceAll.append("Yes", null);
      this.f8.append((Item)this.cgReplaceAll);
      this.f8.addCommand(this.okCommand);
      this.f8.addCommand(this.cancelCommand);
      this.f8.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f8);
    } 
    if (paramCommand == this.goToCommand) {
      this.f6 = new Form("Go to position");
      this.tf = new TextField("Position (0 to " + this.tb.size() + ")", null, 8, 2);
      this.f6.append((Item)this.tf);
      this.f6.addCommand(this.okCommand);
      this.f6.addCommand(this.cancelCommand);
      this.f6.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f6);
    } 
    if (paramCommand == this.topCommand) {
      this.caretPosition = 0;
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.bottomCommand) {
      this.caretPosition = this.tb.size();
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.passwordCommand) {
      setPassword();
      return;
    } 
    if (paramCommand == this.backCommand) {
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramCommand == this.preferencesCommand) {
      this.f5 = new Form("Preferences");
      this.tf = new TextField("Warn before opening large file (0 = no warning)", null, 8, 2);
      this.tf.setString("" + this.largeFileWarning);
      this.f5.append((Item)this.tf);
      this.f5.addCommand(this.okCommand);
      this.f5.addCommand(this.cancelCommand);
      this.f5.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f5);
      return;
    } 
    if (paramCommand == this.helpCommand) {
      this.runMode = 6;
      runThread();
      return;
    } 
    if (paramCommand == this.aboutCommand) {
      this.f9 = new Form("About");
      this.f9.append("jTextLite 1.01");
      this.f9.append("(c) Malcolm Bryant & FreEPOC 2009");
      this.f9.addCommand(this.okCommand);
      this.f9.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f9);
      return;
    } 
    if (paramDisplayable == this.helpText) {
      this.display.setCurrent((Displayable)this.tb);
      this.helpText = null;
      return;
    } 
    if (paramDisplayable == this.f && paramCommand == this.okCommand) {
      this.password = this.tf.getString();
      this.runMode = 4;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f1 && paramCommand == this.okCommand) {
      if (this.tf1.getString().equals(this.tf.getString())) {
        if (this.tf.getString().equals("")) {
          if (this.saveFile.endsWith("#"))
            this.saveFile = this.saveFile.substring(0, this.saveFile.length() - 1); 
          if (this.saveFile.endsWith("#.rem"))
            this.saveFile = this.saveFile.substring(0, this.saveFile.length() - 5); 
        } else {
          if (!this.saveFile.endsWith("#") && !this.saveFile.endsWith("#.rem"))
            this.saveFile += "#"; 
          if (this.cgDelete.getSelectedIndex() == 0) {
            this.deletePlain = true;
          } else {
            this.deletePlain = false;
          } 
        } 
        updateRecentFiles();
        this.password = this.tf.getString();
        this.display.setCurrent((Displayable)this.tb);
        return;
      } 
      this.passwordError++;
      if (this.passwordError == 3)
        destroyApp(true); 
      this.alert.setString("Password error");
      this.display.setCurrent(this.alert, (Displayable)this.f1);
      return;
    } 
    if (paramDisplayable == this.f2 && paramCommand == this.okCommand) {
      this.tb.setString("");
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramDisplayable == this.f3 && paramCommand == this.okCommand) {
      String str = this.cgrf.getString(this.cgrf.getSelectedIndex());
      if (str.equals("(empty)")) {
        this.alert.setString("Unassigned file");
        this.display.setCurrent(this.alert, (Displayable)this.f3);
      } else {
        this.saveFile = str.substring(str.lastIndexOf('/') + 1);
        this.currDirName = str.substring(0, str.lastIndexOf('/') + 1);
        if (this.saveFile.endsWith("#") || this.saveFile.endsWith("#.rem")) {
          getPassword();
        } else {
          this.runMode = 4;
          runThread();
        } 
      } 
      return;
    } 
    if (paramDisplayable == this.f4 && paramCommand == this.okCommand) {
      this.saveFile = this.tf.getString();
      if (this.saveFile.indexOf(".") < 0)
        this.saveFile += ".txt"; 
      this.runMode = 1;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f5 && paramCommand == this.okCommand) {
      this.largeFileWarning = Integer.valueOf(this.tf.getString()).intValue();
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramDisplayable == this.f6 && paramCommand == this.okCommand) {
      this.caretPosition = Integer.valueOf(this.tf.getString()).intValue();
      if (this.caretPosition < 0)
        this.caretPosition = 0; 
      if (this.caretPosition > this.tb.size())
        this.caretPosition = this.tb.size(); 
      this.display.setCurrent((Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f7 && paramCommand == this.okCommand) {
      int i = this.tb.getString().toLowerCase().indexOf(this.tf.getString().toLowerCase());
      if (i < 0) {
        this.alert.setString("String not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.caretPosition = i;
      this.display.setCurrent((Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f8 && paramCommand == this.okCommand) {
      this.caretPosition = this.tb.getCaretPosition();
      byte b = 0;
      do {
        int i = this.tb.getString().toLowerCase().substring(this.caretPosition).indexOf(this.tf.getString().toLowerCase());
        if (i < 0)
          break; 
        this.tb.delete(this.caretPosition + i, this.tf.getString().length());
        this.tb.insert(this.tf1.getString(), this.caretPosition + i);
        b++;
        this.caretPosition += i + this.tf1.getString().length();
      } while (this.cgReplaceAll.getSelectedIndex() != 0);
      this.alert.setString(b + " replaced");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f9 && paramCommand == this.okCommand) {
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramCommand == this.cancelCommand) {
      this.alert.setString("Cancelled");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      return;
    } 
  }
}
