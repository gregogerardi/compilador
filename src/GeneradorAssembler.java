import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Vector;

public class GeneradorAssembler {
    private static Vector<String> code = new Vector<>();
    private static String result;

    public GeneradorAssembler() {
    }

    public static void generarAssembler(ArrayList<Terceto> listaTercetos) throws IOException {
        addData();
        addCode(listaTercetos);
        addFinalCode();
        addTablaDeSimbolos();
        codeToTxt();
    }

    private static void addTablaDeSimbolos() {
        int indexV = 3;
        int indexS = 3;
        for (EntradaTablaSimbolos e : AnalizadorLexico.tablaDeSimbolos.values()) {
            if (e.getLexema().substring(0, 1).equals("_") || e.getLexema().substring(0, 1).equals("@")) {                                        //Variables
                code.add(indexV, e.getLexema() + " DD ?");
                indexS++;
            } else if (e.getTipo().equals("String")) {                                              //Strings
                code.add(indexS, e.getLexema() + " db \"" + e.getLexema() + "\", 0");
            } else {
                code.add(3, e.getLexema() + " DW " + e.getLexema());                 //Constantes
                indexV++;
                indexS++;
            }
        }
    }

    private static void codeToTxt() throws IOException {
        File file = new File("assembler.txt");
        Files.deleteIfExists(file.toPath());
        PrintWriter pw = new PrintWriter(file);
        for (int i = 0; i < code.size(); i++) {
            if (!code.get(i).equals("\n"))
                pw.println(code.get(i));
            else
                pw.print(code.get(i));
        }
        pw.close();
    }

    private static void addFinalCode() {
        code.add(String.valueOf('\n'));
        code.add("@LABEL_OVF_PRODUCTO:");
        code.add("invoke MessageBox, NULL, addr mensaje_overflow_producto, addr mensaje_overflow_producto, MB_OK");
        code.add("JMP @LABEL_END");
        code.add("@LABEL_OVF_SUMA:");
        code.add("invoke MessageBox, NULL, addr mensaje_overflow_suma, addr mensaje_overflow_suma, MB_OK");
        code.add("JMP @LABEL_END");
        code.add("@LABEL_DIV_CERO:");
        code.add("invoke MessageBox, NULL, addr mensaje_division_cero, addr mensaje_division_cero, MB_OK");
        code.add("@LABEL_END:");
        code.add("invoke ExitProcess, 0");
        code.add("end start");
    }

    private static void addData() {
        code.add(".MODEL small");
        code.add(".STACK 200h");
        code.add(".DATA");
        code.add("mensaje_overflow_producto db \"ERROR EN TIEMPO DE EJECUCION --> OVERFLOW DETECTADO EN PRODUCTO\", 0");
        code.add("mensaje_overflow_suma db \"ERROR EN TIEMPO DE EJECUCION --> OVERFLOW DETECTADO EN SUMA\", 0");
        code.add("mensaje_division_cero db \"ERROR EN TIEMPO DE EJECUCION --> DIVISION POR CERO DETECTADA\", 0");
    }

    private static String getResult(Terceto t) {
        t.setAuxResultado("@aux" + ListaTercetos.getInstanceOfListaDeTercetos().getIndice(t));
        return ("@aux" + ListaTercetos.getInstanceOfListaDeTercetos().getIndice(t));
    }

    private static void addCode(ArrayList<Terceto> listaTercetos) {
        code.add(String.valueOf('\n'));
        code.add(".code");
        code.add("start:");
        Terceto t;
        ArrayList<Integer> labels = new ArrayList<>();

        for (int i = 0; i < listaTercetos.size(); i++) {
            t = listaTercetos.get(i);

            //guardo labelTerceto en caso de que tenga vaya ser utilizado por una condicion
            for (int j = 0; j < labels.size(); j++) {
                if (labels.get(j) == i){
                    code.add("@labelTerceto" + i);
                    labels.remove(j);
                }
            }

            //--ARITMETICOS--
            //suma
            if (t.getOperador().equals("+")) {
                if (t.getTipo().equals(EntradaTablaSimbolos.SINGLE)) {
                    //todo arreglar no andan los flags en el coprocesador
                    code.add("FLD " + t.getOperando1ForAssembler());
                    code.add("FLD " + t.getOperando2ForAssembler());
                    code.add("FADD");
                    code.add("FISTP " + getResult(t));
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }
                if (t.getTipo().equals(EntradaTablaSimbolos.LONG)) {
                    code.add("MOV EAX, " + t.getOperando1ForAssembler());
                    code.add("ADD EAX, " + t.getOperando2ForAssembler());
                    code.add("JO @LABEL_OVF_SUMA");
                    code.add("MOV " + getResult(t) + ", EAX");
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }

            }
            //resta
            if (t.getOperador().equals("-")) {
                if (t.getTipo().equals(EntradaTablaSimbolos.SINGLE)) {
                    code.add("FLD " + t.getOperando1ForAssembler());
                    code.add("FLD " + t.getOperando2ForAssembler()); //tener en cuenta que hace ST(1) - ST
                    code.add("FSUB");
                    code.add("FSTP " + getResult(t));
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }
                if (t.getTipo().equals(EntradaTablaSimbolos.LONG)) {
                    code.add("MOV EAX, " + t.getOperando1ForAssembler());
                    code.add("SUB EAX, " + t.getOperando2ForAssembler());
                    code.add("MOV " + getResult(t) + ", EAX");
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }

            }
            //multiplicacion
            if (t.getOperador().equals("*")) {
                if (t.getTipo().equals(EntradaTablaSimbolos.SINGLE)) {
                    //todo arreglar no andan los flags en el coprocesador
                    code.add("FLD " + t.getOperando1ForAssembler());
                    code.add("FLD " + t.getOperando2ForAssembler()); //tener en cuenta que hace ST(1) * ST
                    code.add("FMUL");
                    code.add("FSTP " + getResult(t));
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }
                if (t.getTipo().equals(EntradaTablaSimbolos.LONG)) {
                    code.add("MOV EAX," + t.getOperando1ForAssembler());
                    code.add("IMUL EAX," + t.getOperando2ForAssembler());
                    code.add("JO @LABEL_OVF_PRODUCTO");
                    code.add("MOV " + getResult(t) + ", EAX");
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }

            }
            //division
            if (t.getOperador().equals("/")) {
                if (t.getTipo().equals(EntradaTablaSimbolos.SINGLE)) {
                    //todo arreglar no andan los flags en el coprocesador
                    //chequeo division cero
                    code.add("FLD " + t.getOperando2ForAssembler());
                    code.add("FLDZ");
                    code.add("FSUB");
                    code.add("JZ @LABEL_DIV_CERO");
                    //
                    code.add("FLD " + t.getOperando1ForAssembler());
                    code.add("FLD " + t.getOperando2ForAssembler()); //tener en cuenta que hace ST(1) / ST
                    code.add("FDIV");
                    code.add("FSTP " + getResult(t));
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }
                if (t.getTipo().equals(EntradaTablaSimbolos.LONG)) {
                    //chequeo division cero
                    code.add("MOV EAX," + t.getOperando2ForAssembler());
                    code.add("SUB EAX, 0");
                    code.add("JZ @LABEL_DIV_CERO");
                    //
                    code.add("MOV EAX," + t.getOperando1ForAssembler());
                    code.add("IDIV " + t.getOperando2ForAssembler());
                    code.add("MOV " + getResult(t) + ", EAX");
                    AnalizadorLexico.agregarATablaSimbolos(new EntradaTablaSimbolos(t.getAuxResultado(), t.getTipo()));
                }

            }
            //asignacion
            if (t.getOperador().equals("ASIGNACION")) {
                if (t.getOperando1().getTipo().equals(EntradaTablaSimbolos.LONG)) {
                    code.add("MOV " + t.getOperando1ForAssembler() + ", " + t.getOperando2ForAssembler());
                } else {
                    code.add("FLD " + t.getOperando2ForAssembler());
                    code.add("FSTP " + t.getOperando1ForAssembler());
                }

            }
            //print
            if (t.getOperador().equals("PRINT")) {
                code.add("invoke MessageBox, NULL, addr " + t.getOperando1ForAssembler() + ", addr " + t.getOperando1ForAssembler() + ", MB_OK");
            }

            //comparaciones
            if ((t.getOperador().equals("<")) || (t.getOperador().equals(">")) || (t.getOperador().equals("COMP_MENOR_IGUAL")) || (t.getOperador().equals("COMP_MAYOR_IGUAL")) || (t.getOperador().equals("="))) {
                code.add("MOVE EAX, " + t.getOperando1ForAssembler());
                code.add("CMP EAX, " + t.getOperando2ForAssembler());
            }
            //comparacion menor
            if (t.getOperador().equals("<")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JGL @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
            //comparacion mayor
            if (t.getOperador().equals(">")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JLE @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
            //comparacion menor igual
            if (t.getOperador().equals("COMP_MENOR_IGUAL")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JG @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
            //comparacion mayor igual
            if (t.getOperador().equals("COMP_MAYOR_IGUAL")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JL @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
            //comparacion igual
            if (t.getOperador().equals("=")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JNE @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
            //comparacion distinto
            if (t.getOperador().equals("!=")) {
                Terceto tnext = listaTercetos.get(i + 1);
                code.add("JE @labelTerceto" + (((TercetoDestino) tnext.getOperando2()).destino.toString()));
                labels.add((((TercetoDestino) tnext.getOperando2()).destino));
            }
        }
    }
}

/*
linteger
simple
referenciaAMemoria
desconocido --> asignacion, sentencia control, print
*/

