import jTextLite.jTextLite;

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

/*

2.02 modifications

+ Find goes to correct section OK
+ \n\n to section and remove before apply changes OK
Order of menu OK
+ Chunk size to preferences OK
+ Prev to last OK
+Remember my section and not the current offset\ CANCELED
+Show chunk always CANCELS
+ save last find str, position, instance number

// defects: no Alert after several "next edit"

// 2.02 - stability, enhance find result, changes dialog

// TODO? undo

*/
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
  
  Form f10; // goto
  
  ChoiceGroup cgrf;
  
  ChoiceGroup cgReplaceAll;
  
  ChoiceGroup cgDelete;
  
  TextBox tb;
  
  String tbAll;
  
  TextBox helpText;
  
  TextField tf;
  
  TextField tf1;
  
  TextField tf_find;
  
  TextField tf_repl;
  
  String sfind = "";
  
  String srepl = "";
  
  boolean start = true;
  
  boolean saveMode = false;
  
  boolean deletePlain = false;
  
  Command exitCommand = new Command("Exit", 7, 1);
  
  Command clearCommand = new Command("Clear", 2, 3);
  
  Command okCommand = new Command("OK", 2, 3);
  
  Command browseCommand = new Command("Browse", 2, 3);
  
  Command saveCommand = new Command("Save", 2, 3);
  
  Command newCommand = new Command("New", 2, 3);
  
  Command saveAsCommand = new Command("Save As", 2, 3);
  
  Command selectCommand = new Command("Select", 2, 3);
  
  Command recentCommand = new Command("Recent", 2, 3);
  
  Command findCommand = new Command("Find", 2, 2);

  Command gotoCommand = new Command("Goto Position", 2, 3);
  
  Command replaceCommand = new Command("Replace", 2, 3);
  
  Command topCommand = new Command("Top", 2, 3);
  
  Command cancelCommand = new Command("Cancel", 1, 3);
  
  Command viewCommand = new Command("View", 8, 1);
  
  Command backCommand = new Command("Back", 2, 2);
  
  Command preferencesCommand = new Command("Preferences", 2, 4);
  
  Command helpCommand = new Command("Help", 2, 4);
  
  Command aboutCommand = new Command("About", 2, 5);
  
  Command editApplyCommand = new Command("Edit/ View", 2, 2);
  
  Command editPrevCommand = new Command("Edit Prev", 2, 2);
  
  Command editNextCommand = new Command("Edit Next", 2, 2);
  
  static final String UP_DIRECTORY = "..";
  
  static final String MEGA_ROOT = "/";
  
  static final String SEP_STR = "/";
  
  static final char SEP = '/';
  
  String currDirName = "/";
  
  String saveFile = "";
  
  String[] recentFiles;
  
  Image dirIcon;
  
  Image fileIcon;
  
  Image[] iconList;
  
  int runMode;
  
  int largeFileWarning = 20000;
  
  int caretPosition = 0;
  
  int currSection = 0;
  
  int chunkSize = 750;
  
  int instance = 0;
  
  String changed = "";
  
  boolean warn = false;
  
  String debug = "";
  
  boolean inEdit = false;
  
  int changes = 0;
  
  public jTextLite() {
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
    if (!this.start)
      return; 
    this.start = false;
    openDataStore();
    this.display = Display.getDisplay(this);
    this.alert = new Alert("User Information");
    this.alert.setType(AlertType.CONFIRMATION);
    this.alert.setTimeout(1500);
    this.tb = new TextBox("", null, (this.largeFileWarning == 0) ? 20000 : this.largeFileWarning, 131072);
    this.tb.setInitialInputMode("UCB_ARABIC");
    getData();
    this.tb.setTitle("TextView Lite");
    this.tb.addCommand(this.exitCommand);
    this.tb.addCommand(this.findCommand);
    this.tb.addCommand(this.editApplyCommand);
    this.tb.addCommand(this.editPrevCommand);
    this.tb.addCommand(this.editNextCommand);
    this.tb.addCommand(this.gotoCommand);
    this.tb.addCommand(this.topCommand);
    this.tb.addCommand(this.browseCommand);
    this.tb.addCommand(this.recentCommand);
    this.tb.addCommand(this.clearCommand);
    this.tb.addCommand(this.saveCommand);
    this.tb.addCommand(this.saveAsCommand);
    this.tb.addCommand(this.replaceCommand);
    this.tb.addCommand(this.preferencesCommand);
    this.tb.addCommand(this.helpCommand);
    this.tb.addCommand(this.aboutCommand);
    this.tb.setCommandListener(this);
    this.display.setCurrent((Displayable)this.tb);
    this.runMode = 7;
    runThread();
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
    if (this.runMode == 4) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      if (!this.warn && this.changes > 0) {
        this.warn = true;
        this.alert.setString("Text has " + this.changes + " change(s).\nYou should save it.\n\nLast change " + ((this.changed.length() == 0) ? "(unknown)" : this.changed));
        this.saveFile = "";
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        this.runMode = 7;
      } else {
        showFile(this.saveFile);
      } 
    } 
    if (this.runMode == 5)
      traverseDirectory(this.saveFile); 
    if (this.runMode == 6)
      processHelp(); 
    if (this.runMode != 7)
      return; 
    setCaretPosition();
  }
  
  void setCaretPositionToTop() {
    this.caretPosition = 0;
    this.runMode = 7;
    this.currSection = 0;
    runThread();
  }
  
  void setCaretPosition() {
    try {
      Thread.sleep(500L);
    } catch (InterruptedException interruptedException) {}
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
      list.setCommandListener(this);
      if (fileConnection != null)
        fileConnection.close(); 
      this.display.setCurrent((Displayable)list);
    } catch (IOException iOException) {}
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
    this.debug = "";
    try {
      this.tb.setConstraints(131072);
      this.tb.setTitle("TextView Lite");
    } catch (Exception exception) {}
    try {
      FileConnection fileConnection = (FileConnection)Connector.open("file://localhost/" + this.currDirName + paramString, 1);
      int i = (int)fileConnection.fileSize();
      if (this.largeFileWarning > 0 && i > this.largeFileWarning) {
        this.alert.setString("File exceeds large file limit");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      DataInputStream dataInputStream = fileConnection.openDataInputStream();
      byte[] arrayOfByte = new byte[i];
      int j = dataInputStream.read(arrayOfByte, 0, i);
      dataInputStream.close();
      fileConnection.close();
      if (j == 0) {
        this.alert.setString("Text not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      try {
        this.tb.setString(new String(arrayOfByte, 0, j, "UTF8"));
        this.tbAll = new String(arrayOfByte, 0, j, "UTF8");
      } catch (UnsupportedEncodingException unsupportedEncodingException) {
        this.tb.setString(new String(arrayOfByte, 0, j));
        this.tbAll = new String(arrayOfByte, 0, j);
      } 
      updateRecentFiles();
      this.display.setCurrent((Displayable)this.tb);
      this.currSection = 0;
      this.instance = 0;
      this.changed = "";
      this.changes = 0;
      this.caretPosition = 0;
      this.warn = false;
      saveData();
    } catch (Exception exception) {
      Alert alert = new Alert("Error!", "Can not access file " + paramString + " in directory " + this.currDirName + "\nException: " + exception.getClass().getName(), null, AlertType.ERROR);
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
      fileConnection.create();
      DataOutputStream dataOutputStream = fileConnection.openDataOutputStream();
      try {
        arrayOfByte = this.tbAll.getBytes("UTF8");
      } catch (UnsupportedEncodingException unsupportedEncodingException) {
        arrayOfByte = this.tbAll.getBytes();
      } 
      dataOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
      dataOutputStream.close();
      fileConnection.close();
      updateRecentFiles();
      this.alert.setString("Saved");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.changed = "";
      this.changes = 0;
      this.warn = false;
      saveData();
    } catch (Exception exception) {
      Alert alert = new Alert("Error!", "Can not access file " + this.saveFile + " in directory " + this.currDirName + "\nException: " + exception.getClass().getName(), null, AlertType.ERROR);
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
  
  void initialise() {}
  
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
  
  void openDataStore() {
    try {
      this.rs = RecordStore.openRecordStore("jTextLite", true, 1, true);
    } catch (RecordStoreException recordStoreException) {}
  }
  
  void closeDataStore() {
    if (this.rs == null)
      return; 
    try {
      this.rs.closeRecordStore();
    } catch (RecordStoreException recordStoreException) {}
  }
  
  int getNumRecords() {
    if (this.rs == null)
      return 0; 
    try {
      return this.rs.getNumRecords();
    } catch (RecordStoreException recordStoreException) {
      return 0;
    } 
  }
  
  byte[] toByteArray() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    try {
      dataOutputStream.writeUTF(this.tbAll);
      dataOutputStream.writeInt(this.currSection);
      for (byte b = 0; b < 4; b++)
        dataOutputStream.writeUTF(this.recentFiles[b]); 
      dataOutputStream.writeInt(this.largeFileWarning);
      dataOutputStream.writeInt(this.chunkSize);
      dataOutputStream.writeUTF(this.sfind);
      dataOutputStream.writeInt(this.caretPosition);
      dataOutputStream.writeInt(this.instance);
      dataOutputStream.writeInt(this.changes);
    } catch (IOException iOException) {}
    return byteArrayOutputStream.toByteArray();
  }
  
  void fromByteArray(byte[] paramArrayOfbyte) {
    try {
      DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(paramArrayOfbyte));
      this.tbAll = dataInputStream.readUTF();
      this.tb.setString(this.tbAll);
      this.currSection = dataInputStream.readInt();
      for (byte b = 0; b < 4; b++)
        this.recentFiles[b] = dataInputStream.readUTF(); 
      this.largeFileWarning = dataInputStream.readInt();
      this.chunkSize = dataInputStream.readInt();
      this.sfind = dataInputStream.readUTF();
      this.caretPosition = dataInputStream.readInt();
      this.instance = dataInputStream.readInt();
      this.changes = dataInputStream.readInt();
    } catch (IOException iOException) {}
  }
  
  void getData() {
    try {
      if (this.rs.getNumRecords() == 0)
        return; 
      byte[] arrayOfByte = this.rs.getRecord(1);
      fromByteArray(arrayOfByte);
    } catch (RecordStoreException recordStoreException) {}
  }
  
  void saveData() {
    if (this.rs == null)
      return; 
    try {
      byte[] arrayOfByte = toByteArray();
      if (this.rs.getNumRecords() > 0) {
        this.rs.setRecord(1, arrayOfByte, 0, arrayOfByte.length);
      } else {
        this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
      } 
    } catch (RecordStoreException recordStoreException) {}
  }
  
  int getSize() {
    try {
      return this.rs.getSize();
    } catch (RecordStoreException recordStoreException) {
      return 0;
    } 
  }
  
  void processHelp() {
    char c = ';
    this.helpText = new TextBox("jTextLite Help", null, c, 131072);
    this.helpText.addCommand(this.okCommand);
    this.helpText.setCommandListener(this);
    try {
      DataInputStream dataInputStream = new DataInputStream(getClass().getResourceAsStream("/Text/jTextLiteHelp.txt"));
      if (dataInputStream == null) {
        this.helpText.setString("Help not found");
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
    if (this.tbAll == null)
      this.tbAll = ""; 
    if (paramCommand == this.exitCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
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
    if (paramCommand == this.gotoCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      if (this.tbAll.length() == 0) {
        this.alert.setString("Goto Position:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.f10 = new Form("Goto Position");
      this.tf = new TextField("New caret position", null, 5, 2);
      this.tf.setString("" + this.caretPosition);
      this.f10.append((Item)this.tf);
      this.f10.addCommand(this.okCommand);
      this.f10.addCommand(this.cancelCommand);
      this.f10.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f10);
      return;
    } 
    if (paramCommand == this.browseCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      this.saveMode = false;
      this.runMode = 3;
      runThread();
      return;
    } 
    if (paramCommand == this.saveAsCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      this.saveMode = true;
      this.runMode = 3;
      runThread();
      return;
    } 
    if (paramCommand == this.saveCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      this.runMode = 1;
      runThread();
      return;
    } 
    if (paramCommand == this.viewCommand) {
      List list = (List)paramDisplayable;
      String str = list.getString(list.getSelectedIndex());
      if (this.saveFile.equals(str)) {
        this.alert.setString(str + " already loaded");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.saveFile = str;
      if (str.endsWith("/") || str.equals("..")) {
        this.runMode = 5;
        runThread();
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
      if (this.tb.getConstraints() == 0)
        apply(); 
      if (this.tbAll.length() > 0) {
        this.f7 = new Form("Find string");
        this.tf_find = new TextField("String (case insensitive)", this.sfind, 32, 0);
        this.f7.append((Item)this.tf_find);
        this.f7.addCommand(this.okCommand);
        this.f7.addCommand(this.cancelCommand);
        this.f7.setCommandListener(this);
        this.display.setCurrent((Displayable)this.f7);
      } else {
        this.alert.setString("Find:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
    } 
    if (paramCommand == this.replaceCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      if (this.tbAll.length() > 0) {
        this.f8 = new Form("Replace");
        this.tf_find = new TextField("Old String", this.sfind, 32, 0);
        this.f8.append((Item)this.tf_find);
        this.tf_repl = new TextField("New String", this.srepl, 32, 0);
        this.f8.append((Item)this.tf_repl);
        this.cgReplaceAll = new ChoiceGroup("Replace All", 1);
        this.cgReplaceAll.append("No", null);
        this.cgReplaceAll.append("Yes", null);
        this.f8.append((Item)this.cgReplaceAll);
        this.f8.addCommand(this.okCommand);
        this.f8.addCommand(this.cancelCommand);
        this.f8.setCommandListener(this);
        this.display.setCurrent((Displayable)this.f8);
      } else {
        this.alert.setString("Replace:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
    } 
    if (paramCommand == this.topCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      this.tb.setString((new StringBuffer(this.tbAll)).toString());
      this.alert.setString("Top issued\nWas at caret position: " + this.caretPosition + "\nChunk: " + (this.currSection + 1));
      this.currSection = 0;
      this.instance = 0;
      this.caretPosition = 0;
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      return;
    } 
    if (paramCommand == this.backCommand) {
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramCommand == this.preferencesCommand) {
      this.f5 = new Form("Preferences");
      this.tf = new TextField("Warn before opening large file (0 = no warning)", null, 5, 2);
      this.tf.setString("" + this.largeFileWarning);
      this.tf1 = new TextField("Chunk size", null, 4, 2);
      this.tf1.setString("" + this.chunkSize);
      this.f5.append((Item)this.tf);
      this.f5.append((Item)this.tf1);
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
      this.f9.append("jTextLite 2.02");
      this.f9.append("File size: " + this.tbAll.length() + "\nCaret position: " + this.caretPosition + "\nChunk: " + (this.currSection + 1) + "/" + ((this.tbAll.length() + this.chunkSize - 1) / this.chunkSize));
      this.f9.append("(c) Malcolm Bryant & FreEPOC 2009");
      this.f9.append("Modified by me (Shahar) - to make it work."); // \nRemoved: Encryption"); // 2000 chunk maximum // on large files, with replace, find
      if (this.debug.length() > 0)
        this.f9.append("debug =" + this.debug); 
      this.f9.addCommand(this.okCommand);
      this.f9.setCommandListener(this);
      this.display.setCurrent((Displayable)this.f9);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.editNextCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      if ((this.currSection + 1) * this.chunkSize < this.tbAll.length()) {
        this.currSection++;
        // this.alert.setString("Edit chunk '" + currSection + "\nApply Edit to change, Top to restart");
      } else {
        // this.alert.setString("Edit chunk 'last'\nApply Edit to change, Top to restart");
        this.currSection = 0;
      } 
      edit();
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.editPrevCommand) {
      if (this.tb.getConstraints() == 0)
        apply(); 
      if ((this.currSection - 1) * this.chunkSize >= 0) {
        this.currSection--;
      } else {
        this.currSection = (this.tbAll.length() - 1) / this.chunkSize;
      } 
      edit();
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramCommand == this.editApplyCommand) {
      if (this.tb.getConstraints() == 131072) {
        edit();
        this.display.setCurrent(this.alert, (Displayable)this.tb);
      } else {
        this.display.setCurrent((Displayable)this.tb);
        apply();
      } 
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.helpText) {
      this.display.setCurrent((Displayable)this.tb);
      this.helpText = null;
      return;
    } 
    if (paramDisplayable == this.f2 && paramCommand == this.okCommand) { // clearCommand
      if (this.tb.getConstraints() == 0)
        apply(); 
      if (this.tbAll.length() == 0) {
        this.alert.setString("Clear:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      if (!this.warn && this.changes > 0) {
        this.warn = true;
        this.alert.setString("Text has " + this.changes + " change(s).\nYou should save it.\n\nLast change " + ((this.changed.length() == 0) ? "(unknown)" : this.changed));
        this.saveFile = "";
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.tbAll = "";
      this.changed = "";
      this.changes = 0;
      this.saveFile = "";
      this.warn = false;
      this.instance = 0;
      this.tb.setString("");
      this.tb.setTitle("TextView Lite");
      this.tb.setConstraints(131072);
      this.alert.setString("Clear issued");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.caretPosition = 0;
      this.currSection = 0;
      saveData();
      return;
    } 
    if (paramDisplayable == this.f3 && paramCommand == this.okCommand) { // recent
      String str = this.cgrf.getString(this.cgrf.getSelectedIndex());
      if (str.equals("(empty)")) {
        this.alert.setString("Unassigned file");
        this.display.setCurrent(this.alert, (Displayable)this.f3);
      } else {
        this.saveFile = str.substring(str.lastIndexOf('/') + 1);
        this.currDirName = str.substring(0, str.lastIndexOf('/') + 1);
        this.runMode = 4;
        runThread();
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
    if (paramDisplayable == this.f5 && paramCommand == this.okCommand) { // preferences
      this.largeFileWarning = Integer.valueOf(this.tf.getString()).intValue();
      this.chunkSize = Integer.valueOf(this.tf1.getString()).intValue();
      if (this.chunkSize < 5)
        this.chunkSize = 5; 
      this.currSection = 0;
      saveData();
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramDisplayable == this.f10 && paramCommand == this.okCommand) { // goto
      if (this.tf.getString().length() == 0) {
        this.alert.setString("Goto:\nCaret Position not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      this.caretPosition = Integer.valueOf(this.tf.getString()).intValue();
      if (this.caretPosition >= this.tbAll.length())
        this.caretPosition = this.tbAll.length() - 1; 
      this.currSection = (this.caretPosition + 1) / this.chunkSize;
      saveData();
      byte b = 100;
      int i = this.caretPosition - b;
      if (i < 0)
        i = 0; 
      int j = this.caretPosition + b;
      if (j >= this.tbAll.length())
        j = this.tbAll.length(); 
      this.alert.setString(this.tbAll.substring(i, j));
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      return;
    } 
    if (paramDisplayable == this.f7 && paramCommand == this.okCommand) { // findCommand
      this.sfind = this.tf_find.getString();
      if (this.sfind.length() == 0) {
        this.alert.setString("Find:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      saveData(); // save the sfind
      int i = this.tbAll.toLowerCase().substring(this.caretPosition).indexOf(this.tf_find.getString().toLowerCase());
      if (i < 0) {
        this.alert.setString("String '" + this.tf_find.getString() + "' not found\nWrap around...");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        this.caretPosition = 0;
        this.instance = 0;
        return;
      } 
      this.instance++;
      byte b = 30;
      int j = this.caretPosition + i - b;
      if (j < 0)
        j = 0; 
      int k = this.caretPosition + this.tf_find.getString().length() + i + b;
      if (k >= this.tbAll.length())
        k = this.tbAll.length(); 
      this.currSection = (this.caretPosition + i + 1) / this.chunkSize;
      this.alert.setString("Instance " + this.instance + " on " + ((this.caretPosition + i + 1) * 100 / this.tbAll.length()) + "%\n\n" + this.tbAll.substring(j, this.caretPosition + i) + "**" + this.sfind + "**" + this.tbAll.substring(this.caretPosition + i + this.sfind.length(), k) + "\n\nChunk is " + (this.currSection + 1) + "\n(" + ((this.caretPosition + i + 1) % this.chunkSize) + " " + ((this.caretPosition + i + 1) % this.chunkSize * 100 / this.chunkSize) + "%)");
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.caretPosition += 1 + i;
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f8 && paramCommand == this.okCommand) { // replaceCommand
      this.sfind = this.tf_find.getString();
      this.srepl = this.tf_repl.getString();
      if (this.sfind.length() == 0) {
        this.alert.setString("Replace:\nText not found");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        return;
      } 
      byte b = 0;
      do {
        int i = this.tbAll.toLowerCase().substring(this.caretPosition).indexOf(this.tf_find.getString().toLowerCase());
        if (i < 0)
          break; 
        this.changed = "at chunk" + ((i + this.caretPosition) % this.chunkSize + 1) + " " + ((i + this.caretPosition) % this.chunkSize * 100 / this.chunkSize) + "%" + ": was '" + this.tf_find.getString() + "', now '" + this.tf_repl.getString() + "'";
        this.changes++;
        this.warn = false;
        StringBuffer stringBuffer = new StringBuffer(this.tbAll.substring(0, this.caretPosition + i));
        stringBuffer.append(this.tf_repl.getString());
        stringBuffer.append(this.tbAll.substring(this.caretPosition + i + this.tf_find.getString().length(), this.tbAll.length()));
        this.tbAll = stringBuffer.toString();
        b++;
        this.caretPosition += i + this.tf_repl.getString().length();
      } while (this.cgReplaceAll.getSelectedIndex() != 0);
      if (b > 0) {
        this.tb.setString((new StringBuffer(this.tbAll)).toString());
      } else {
        this.alert.setString("String '" + this.tf_find.getString() + "' not found\nWrap around...");
        this.display.setCurrent(this.alert, (Displayable)this.tb);
        this.caretPosition = 0;
        return;
      } 
      this.alert.setString("Replaced: " + b);
      this.display.setCurrent(this.alert, (Displayable)this.tb);
      this.runMode = 7;
      runThread();
      return;
    } 
    if (paramDisplayable == this.f9 && paramCommand == this.okCommand) {
      this.display.setCurrent((Displayable)this.tb);
      return;
    } 
    if (paramCommand != this.cancelCommand)
      return; 
    this.alert.setString("Cancelled");
    this.display.setCurrent(this.alert, (Displayable)this.tb);
  }
  
  private void edit() {
    try {
        this.alert.setString("Chunk " + (currSection+1) + "/" + (tbAll.length()+chunkSize-1) / chunkSize);
        int length = chunkSize;
        if (chunkSize*currSection + length > tbAll.length())
          length = tbAll.length() - chunkSize*currSection;
        tb.setString(new StringBuffer(tbAll.substring(chunkSize*currSection, chunkSize*currSection + length)).toString() + "\n\n") ;

        inEdit = false;
        tb.setTitle("EDIT MODE");
    tb.setConstraints(TextField.ANY);
//        tb.setMaxSize(chunkSize+10);
        debug = "";
        inEdit = true;
    }
    catch (Exception e3) {
        this.alert.setString("Edit again");
//            debug += " on edit " + e3.getClass().toString(); // getMessage(); // Class().getName();
    }
  }

  private void apply() {

    if (!inEdit) {
        this.tb.setString(new StringBuffer(tbAll).toString());
        this.alert.setString("Edit again.\n(last time:)");
        this.display.setCurrent(this.alert, this.tb);
    }
    else {
      try{
        // start
        StringBuffer s = new StringBuffer(tbAll.substring(0, chunkSize*currSection));
        // middle
        int twoSlashN = tb.getString().length();
        if (tb.getString().endsWith("\n\n"))
          twoSlashN -= "\n\n".length();
        else if(tb.getString().endsWith("\n"))
          twoSlashN -= "\n".length();
        s.append(tb.getString().substring(0, twoSlashN));

        // end
        if (tbAll.length() > chunkSize*(currSection+1))
          s.append(tbAll.substring(chunkSize*(currSection+1)));

        String diff = compareTo(tbAll, s.toString());

        if (/*! changed && */ //0 != tbAll.compareTo(s.toString())) {
          diff.length() > 0) {
          changed = diff;
          warn = false;
          this.tbAll = s.toString();
          saveData();
        }
        else
          this.tbAll = s.toString();

        this.tb.setString(s.toString());
      }
      catch (Exception e3) {
        // debug += " on apply (new) " + e3.getMessage();
      }
    }

    tb.setTitle("TextView Lite");
    tb.setConstraints(TextField.UNEDITABLE);
  }

  public String compareTo(String thi, String anotherString)
  {
    StringBuffer b= new StringBuffer("at ");
    int len1 = thi.length();
    int len2 = anotherString.length();
    int n = Math.min(len1, len2);
    int i = 0;

    while (n-- != 0) {
      char c1 = thi.charAt(i++);
      char c2 = anotherString.charAt(i++);
      if (c1 != c2) {
        changes++;
        b.append("" + i);
        b.append(" chunk " + ((i+1)/  chunkSize + 1) + " " + (i% chunkSize)*100/chunkSize + "%");
        b.append(": was ").append(thi.substring(i-1, i)).append("', now '").append(anotherString.substring(i-1, i)).append( "'");
        caretPosition = i;
        return b.toString();
      }
    }

    if (len1 - len2 != 0) {
        changes++;
        b.append("" + i);
        b.append(" chunk " + ((i+1)/  chunkSize + 1) + " " + (i% chunkSize)*100/chunkSize + "%");
        b.append(": diff' length");
        caretPosition = i;
        return b.toString();
    }

    return changed;
  }

}