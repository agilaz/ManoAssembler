import java.util.*;
import java.io.*;
//TODO: Format output properly, implement file i/o,
//check for syntax errors instead of assuming proper syntax.

/*
Line structure (ignoring comments):

<Line> := <Label> <Statement> | <Statement> | λ

<Statement> := <Command> | <Value> | <Org statement> | END

<Org statement> := ORG <address>

<Comand> := <Memory Instruction> <Label> | <Memory Instruction> <Label> I |
          | <Register Instruction> | <IO Instruction>

<Memory Instruction> := AND | ADD | LDA | STA | BUN | BSA | ISZ
<Register Instruction> := CLA | CLE | CMA | CME | CIR | CIL | INC |
                        | SPA | SNA | SZA | SZE | HLT
<IO Instruction> := INP | OUT | SKI | SKO | ION | IOF

<Label> := REGEX: [^\s,]+

<Value> := DEC <dec number> | HEX <hex number>

<Dec number> := REGEX: -?[0-9]+
<Hex number> := REGEX: [0-9A-Fa-f]+

<address> := REGEX: [0-9A-Fa-f]{3}
*/

public class ManoAssembler {
  static int currentAddress = 0;
  static ArrayList<String> originalCommands = new ArrayList<String>();
  static ArrayList<ArrayList<String>> splitCommands = new ArrayList<ArrayList<String>>();
  static ArrayList<String> hexCodes = new ArrayList<String>();
  static ArrayList<String> numberedCommands = new ArrayList<String>();
  static HashMap<String, String> labels = new HashMap<String, String>();
  public static void main(String[] args) {
    if(args.length < 1) {
      System.out.println("Expected command-line argument; no input file specified.");
      return;
    }
    String inputName = args[0];
    Scanner in = null;
    try {
      in = new Scanner(new BufferedReader(new FileReader(inputName)));
      while (in.hasNextLine()) {
        String command = in.nextLine();
        //commandsNew.add(new ArrayList<String>(Arrays.asList(in.nextLine().trim().split("[\\s,]+"))));
        originalCommands.add(command);
        command = command.trim();
        if(command.contains("/"))
          command = command.substring(0, command.indexOf("/"));
        ArrayList<String> splitCommand = new ArrayList<String>(Arrays.asList(command.split("[\\s,]+")));
        splitCommand.removeAll(Collections.singleton(""));
        //if (splitCommand.isEmpty()) System.out.println(splitCommand + " is empty.");
        splitCommands.add(splitCommand);
        //for(String s: splitCommand) System.out.println("*"+s+"*");
        //System.out.println(splitCommands.get(splitCommands.size()-1));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
        if (in != null) {
            in.close();
        }
    }
    
    //First pass: associate labels with addresses
    //System.out.println("Resolving addresses:");
    for (ArrayList<String> splitCommand : splitCommands) {
      if(splitCommand.isEmpty()) continue;
      String thisWord = splitCommand.get(0);
      if (!Symbol.isReserved(thisWord)) {
        Symbol.addLabel(thisWord, currentAddress);
        splitCommand.remove(thisWord);
      }
      thisWord = splitCommand.get(0);
      //Start writing commands at different address
      if (thisWord.equals("ORG")) {
        int orgPos = Integer.parseInt(splitCommand.get(1), 16);
        reposition(orgPos);
      }
      currentAddress++;
    }
    
    currentAddress = 0;
    //Second pass: generate hexCodes from commands
    for (int i = 0; i < splitCommands.size(); i++) {
      ArrayList<String> splitCommand = splitCommands.get(i);
      if(splitCommand.size() == 0) {
        numberedCommands.add("");
        continue;
      }
      //Addressing mode
      //boolean isDirect = command.endsWith("I") ? false : true;
      
      int hexCode = 0x0000;
      //String hexCode = ""; //Full 4-digit hex code for the current instruction
      //String address = ""; //3-bit hex code for the address, used for building memory-ref hexCodes
      
      //System.out.println("Command: " + splitCommand);
      String thisWord = splitCommand.get(0);
      //Change writing address
      if (thisWord.equals("ORG")) {
        int orgPos = Integer.parseInt(splitCommand.get(1), 16);
        reposition(orgPos);
        numberedCommands.add("\t" + originalCommands.get(i));
        continue;
      }
      else if (Symbol.isCommand(thisWord)) {
        Symbol s = Symbol.find(thisWord);
        hexCode = s.value;
        if (s.isMemoryReference()) {
          Symbol label = Symbol.find(splitCommand.get(1));
          hexCode += label.value;
          if (splitCommand.indexOf("I") > 0) {
            hexCode += 0x8000;
          }
        }
      }
      else if (thisWord.equals("DEC")) {
        hexCode = Integer.parseInt(splitCommand.get(1));
      }
      else if (thisWord.equals("HEX")) {
        hexCode = Integer.parseInt(splitCommand.get(1), 16);
      }
      String hexString = makeHexString(hexCode, 4);
      //System.out.println("Hex string: " + hexString);
      //In case the arrayList is full, expand by adding XXXX
      while(hexCodes.size() <= currentAddress) {
      hexCodes.add("XXXX");
      }
      hexCodes.set(currentAddress, hexString);
      //Go to next address
      String currentHexAddress = makeHexString(currentAddress, 3);
      numberedCommands.add(currentHexAddress + ": " + hexString + "\t" + originalCommands.get(i));
      currentAddress++;
    }
    
    String numberedOut = inputName;
    int periodPos = numberedOut.indexOf(".");
    if (periodPos >= 0) {
      numberedOut = numberedOut.substring(0, periodPos) + "-numbered" + numberedOut.substring(periodPos,numberedOut.length());
    } else numberedOut += "-numbered";
    writeToFile(numberedOut, numberedCommands, System.lineSeparator());
    
    String assembledOut = inputName;
    periodPos = assembledOut.indexOf(".");
    if (periodPos >= 0) {
      assembledOut = assembledOut.substring(0, periodPos) + "-assembled" + assembledOut.substring(periodPos,assembledOut.length());
    } else assembledOut += "-assembled";
    writeToFile(assembledOut, hexCodes, " ");
    
    for (String l : numberedCommands) {
      System.out.println(l);
    }
    for (String h : hexCodes) {
      //Print hex codes separated by spaces
      System.out.print(h + " ");
    }
    System.out.println();
  }
  //Change position in code, make sure that the array is full up to that position
  private static void reposition(int position) {
    currentAddress = position;
    //Fill the array up until the position
    while(hexCodes.size() <= position) {
      hexCodes.add("XXXX");
    }
  }
  
  private static String makeHexString(int inNumber, int stringLength) {
    String hexString = Integer.toHexString(inNumber).toUpperCase();
    while (hexString.length() > stringLength) hexString = hexString.substring(1, hexString.length());
    while (hexString.length() < stringLength) {
      if (inNumber >= 0)
        hexString = "0" + hexString;
      else
        hexString = "F" + hexString;
    }
    return hexString;
  }
  
  private static void writeToFile(String fileName, ArrayList<String> content, String delimiter) {
    Writer writer = null;
    try {
      writer = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(fileName), "utf-8"));
      for(String s : content) {
        writer.write(s);
        writer.write(delimiter);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {writer.close();} catch(IOException e) {e.printStackTrace();}
    }
  }
}