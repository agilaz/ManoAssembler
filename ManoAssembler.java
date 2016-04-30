import java.util.*;
import java.io.*;
public class ManoAssembler {
  static int currentAddress = 0;
  static ArrayList<String> commands = new ArrayList<String>();
  static ArrayList<String> hexCodes = new ArrayList<String>();
  static HashMap<String, String> labels = new HashMap<String, String>();
  public static void main(String[] args) {
    Scanner in = null;
    try {
      in = new Scanner(new BufferedReader(new FileReader(args[0])));

      while (in.hasNextLine()) {
          commands.add(in.nextLine());
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
        if (in != null) {
            in.close();
        }
    }
    
    //First pass: associate labels with addresses
    for (String command:commands) {
      
      //Start writing commands at different address
      if (command.contains("ORG")) {
        //Grab new address (assumed to start after "ORG ")
        int start = command.indexOf("ORG")+4;
        int end = command.indexOf(" ", start);
        if (end == -1) end = command.length();
        int orgPos = Integer.parseInt(command.substring(start, end), 16);
        reposition(orgPos);
      }
      //If the current line is labelled
      //(Labels are assumed to be followed by commas)
      if (command.contains(",")) {
        //Assign the label to the current memory address
        String label = command.substring(0,command.indexOf(","));
        System.out.println("Label: " + label + ", position: " + currentAddress);
        labels.put(label, makeHexAddress(currentAddress));
      }
      currentAddress++;
    }
    
    currentAddress = 0;
    //Second pass: generate hexCodes from commands
    for (String command:commands) {
      //Ignore comments
      if (command.contains("/")) {
        command = command.substring(0, command.indexOf("/"));
      }
      //Trim ending whitespace
      while(command.endsWith(" ")) {
        command = command.substring(0, command.length()-1);
      } 
      //Addressing mode
      boolean isDirect = command.endsWith("I") ? false : true;
      
      String hexCode = ""; //Full 4-digit hex code for the current instruction
      String address = ""; //3-bit hex code for the address, used for building memory-ref hexCodes
      
      System.out.println("Command: " + command);
      
      //Change writing address
      if (command.toUpperCase().contains("ORG")) {
        int start = command.indexOf("ORG")+4;
        int end = command.indexOf(" ", start);
        if (end == -1) end = command.length();
        int orgPos = Integer.parseInt(command.substring(start, end), 16);
        reposition(orgPos);
      }
      // if (command.contains(",")) {
        // String label = command.substring(0,command.indexOf(","));
        // System.out.println("Label: " + label);
      // }
      
      //Switch actual command to hex code
      //Memory-reference instructions
      if (command.contains("AND")) {
        System.out.println("Command: AND");
        //Generate opcode and I-bit
        hexCode += (isDirect) ? "0" : "8";
        //Convert label to address, if the label exists
        address = resolveAddress(command, "AND");
        if (address == null) {
          //Invalid label; stop generating bytecode
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("ADD")) {
        System.out.println("Command: ADD");
        hexCode += (isDirect) ? "1" : "9";
        address = resolveAddress(command, "ADD");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("LDA")) {
        System.out.println("Command: LDA");
        hexCode += (isDirect) ? "2" : "A";
        address = resolveAddress(command, "LDA");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("STA")) {
        System.out.println("Command: STA");
        hexCode += (isDirect) ? "3" : "B";
        address = resolveAddress(command, "STA");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("BUN")) {
        System.out.println("Command: BUN");
        hexCode += (isDirect) ? "4" : "C";
        address = resolveAddress(command, "BUN");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("BSA")) {
        System.out.println("Command: BSA");
        hexCode += (isDirect) ? "5" : "D";
        address = resolveAddress(command, "BSA");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      } else if (command.contains("ISZ")) {
        System.out.println("Command: ISZ");
        hexCode += (isDirect) ? "6" : "E";
        address = resolveAddress(command, "ISZ");
        if (address == null) {
          System.out.println("Could not resolve address of the given label. Command:\n" + command);
          return;
        }
        hexCode += address;
      }
      //Register-reference instructions
      else if (command.contains("CLA")) {
        hexCode = "7800";
      } else if (command.contains("CLE")) {
        hexCode = "7400";
      } else if (command.contains("CMA")) {
        hexCode = "7200";
      } else if (command.contains("CME")) {
        hexCode = "7100";
      } else if (command.contains("CIR")) {
        hexCode = "7080";
      } else if (command.contains("CIL")) {
        hexCode = "7040";
      } else if (command.contains("INC")) {
        hexCode = "7020";
      } else if (command.contains("SPA")) {
        hexCode = "7010";
      } else if (command.contains("SNA")) {
        hexCode = "7008";
      } else if (command.contains("SZA")) {
        hexCode = "7004";
      } else if (command.contains("SZE")) {
        hexCode = "7002";
      } else if (command.contains("HLT")) {
        hexCode = "7001";
      }
      //IO instructions
      else if (command.contains("INP")) {
        hexCode = "F800";
      } else if (command.contains("OUT")) {
        hexCode = "F400";
      } else if (command.contains("SKI")) {
        hexCode = "F200";
      } else if (command.contains("SKO")) {
        hexCode = "F100";
      } else if (command.contains("ION")) {
        hexCode = "F080";
      } else if (command.contains("IOF")) {
        hexCode = "F040";
      }
      //Numbers
      if (command.contains("DEC")) {
        //Convert decimal number to two's complement (hexidecimal representation)
        hexCode = decToHex(command);
      } else if (command.contains("HEX")) {
        //Find actual number, assumed to start after "HEX "
        int numberStartIndex = command.indexOf("HEX") + 4;
        int numberEndIndex = command.indexOf(" ", numberStartIndex);
        if (numberEndIndex == -1) numberEndIndex = command.length();
        hexCode = command.substring(numberStartIndex, numberEndIndex);
        //Pad with zeros to make into 16 bit number
        while(hexCode.length() < 4) {
          hexCode = "0" + hexCode; //TODO: Fix for padding negative numbers?
        }
        //Trim extra leading digits (i.e. FFFFF1B5 -> F1B5)
        while(hexCode.length() > 4) {
          hexCode = hexCode.substring(1,hexCode.length());
        }
      }
      //In case the arrayList is full, expand by adding XXXX
      while(hexCodes.size() <= currentAddress) {
      hexCodes.add("XXXX");
      }
      //If a hexCode was made add it at the given address
      if (hexCode.length() > 0) hexCodes.set(currentAddress, hexCode);
      //Go to next address
      currentAddress++;
    }
    
    for(String h : hexCodes) {
      //Print hex codes separated by spaces
      System.out.print(h + " ");
    }
    System.out.println();
  }
  //Change position in code, make sure that the array is full up to that position
  private static void reposition(int position) {
    //Set currentAddress to position-1 (because each loop increments position by 1 at the end)
    currentAddress = position-1;
    //Fill the array up until the position
    while(hexCodes.size() <= position) {
      hexCodes.add("XXXX");
    }
  }
  
  //Grab the label from a command, translate it to the corresponding hex address
  private static String resolveAddress(String command, String commandType) {
    //Assume that the label starts after "<command> "
    int labelStartIndex = command.indexOf(commandType) + commandType.length() + 1;
    //Label ends at the next space or at the end of the string
    int labelEndIndex = command.indexOf(" ", labelStartIndex);
    if (labelEndIndex == -1) labelEndIndex = command.length();
    String label = command.substring(labelStartIndex, labelEndIndex);
    //Grab the address corresponding to this label (null if this label does not exist)
    String address = labels.get(label);
    System.out.println("Label: " + label + ", address: " + address);
    return address;
  }
  
  //Convert decimal number into hexidecimal address (string)
  private static String makeHexAddress(int address) {
    String hexAddress = Integer.toHexString(address);
    //Pad with zeros and remove leading digits to generate a 12-bit hex address
    while (hexAddress.length() < 3) hexAddress = "0" + hexAddress;
    while (hexAddress.length() > 3) hexAddress = hexAddress.substring(1, hexAddress.length());
    hexAddress = hexAddress.toUpperCase();
    return hexAddress;
  }
  
  //Convert a decimal number into a hexidecimal string
  private static String decToHex(String command) {
    //Assume that the number starts after "DEC " and ends at the next space (or the end of the string)
    int numberStartIndex = command.indexOf("DEC") + 4;
    int numberEndIndex = command.indexOf(" ", numberStartIndex);
    if (numberEndIndex == -1) numberEndIndex = command.length();
    int dec = Integer.parseInt(command.substring(numberStartIndex, numberEndIndex));

    String hex = Integer.toHexString(dec);
    //Pad with zeros if necessary
    while(hex.length() < 4) {
      hex = "0" + hex;
    }
    //Trim away leading digits if necessary (i.e. FFFFF1B5 -> F1B5)
    while(hex.length() > 4) {
      hex = hex.substring(1,hex.length());
    }
    hex = hex.toUpperCase();
    System.out.println(command + " -> HEX " + hex);
    return hex;
  }
}