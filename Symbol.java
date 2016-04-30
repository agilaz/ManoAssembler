import java.util.ArrayList;
public class Symbol {
  enum SymbolType {
    LABEL,
    LABEL_UNRESOLVED,
    MEM,
    REG,
    IO,
    OTHER
  }
  
  String name = "";
  SymbolType type = SymbolType.OTHER;
  int value = 0;
  
  public Symbol(String name, SymbolType type, int value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }
  
  public makeIndirect() {
    if(this.type == SymbolType.MEM) this.value += 0x8000;
  }
  
  static ArrayList<Symbol> table = new ArrayList<Symbol>();
  
  static {
    table.add(new Symbol("AND", SymbolType.MEM, 0x0000));
		table.add(new Symbol("ADD", SymbolType.MEM, 0x1000));
		table.add(new Symbol("LDA", SymbolType.MEM, 0x2000));
		table.add(new Symbol("STA", SymbolType.MEM, 0x3000));
		table.add(new Symbol("BUN", SymbolType.MEM, 0x4000));
		table.add(new Symbol("BSA", SymbolType.MEM, 0x5000));
		table.add(new Symbol("ISZ", SymbolType.MEM, 0x6000));
                   
		table.add(new Symbol("CLA", SymbolType.REG, 0x7800));
		table.add(new Symbol("CLE", SymbolType.REG, 0x7400));
		table.add(new Symbol("CMA", SymbolType.REG, 0x7200));
		table.add(new Symbol("CME", SymbolType.REG, 0x7100));
		table.add(new Symbol("CIR", SymbolType.REG, 0x7080));
		table.add(new Symbol("CIL", SymbolType.REG, 0x7040));
		table.add(new Symbol("INC", SymbolType.REG, 0x7020));
		table.add(new Symbol("SPA", SymbolType.REG, 0x7010));
		table.add(new Symbol("SNA", SymbolType.REG, 0x7008));
		table.add(new Symbol("SZA", SymbolType.REG, 0x7004));
		table.add(new Symbol("SZE", SymbolType.REG, 0x7002));
		table.add(new Symbol("HLT", SymbolType.REG, 0x7001));
                    
		table.add(new Symbol("INP", SymbolType.IO, 0xF800));
		table.add(new Symbol("OUT", SymbolType.IO, 0xF400));
		table.add(new Symbol("SKI", SymbolType.IO, 0xF200));
		table.add(new Symbol("SKO", SymbolType.IO, 0xF100));
		table.add(new Symbol("ION", SymbolType.IO, 0xF080));
		table.add(new Symbol("IOF", SymbolType.IO, 0xF040));

		table.add(new Symbol("ORG", SymbolType.OTHER, 0));
		table.add(new Symbol("DEC", SymbolType.OTHER, 0));
		table.add(new Symbol("HEX", SymbolType.OTHER, 0));
  }
  
  static Symbol find(String name) {
    for (Symbol s : table) {
      if (s.name.equals(name)) {
        return s;
      }
    }
    return null;
  }
  static boolean add(String name, SymbolType type, int value) {
    if(find(name) != null) {
      return false;
    } else {
      table.add(new Symbol(name, type, value));
      return true;
    }
  }
  
  static boolean remove(String name) {
    for (Symbol s : table) {
      if (s.name.equals(name)) {
        table.remove(s);
        return true;
      }
    }
    return false;
  }
  
  static void addLabel(String name) {
    table.add(new Symbol(name, SymbolType.LABEL_UNRESOLVED, 0));
  }
  static void addLabel(String name, int value) {
    if(find(name) != null) remove(name);
    table.add(new Symbol(name, SymbolType.LABEL, value));
  }
}