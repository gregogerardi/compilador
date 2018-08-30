package compilador;

	/*
 * Identificadores cuyos nombres pueden tener hasta 15caracteres de longitud. El primer car�cter debe ser
una letra, y el resto pueden ser letras, d�gitos y �_�. Los identificadores con longitud mayor ser�n truncados
y esto se informar� como Warning.Las letras utilizadas en los nombres de identificador pueden ser
may�sculas o min�sculas, y el lenguaje NO ser� case sensitive. Entonces, el identificador MyVariable, ser�
igual a myvariable.
� Constantes correspondientes alostemas particulares asignados a cada grupo. Para aquellos grupos cuyos
temas asignados incluyan constantes tales que, el rango de uno de los tipos est� incluido en el rango del
otro, considerar que, una constante inclu�da en el rango m�s acotado, ser� del tipo m�s chico.
Ejemplos:
� INT y LONG. La constante 123 ser� de tipo INT.
� UINT y ULONG. La constante 25 ser� de tipo UINT.
� FLOAT y DOUBLE. La constante 1.234 ser� de tipo FLOAT.
� Operadores aritm�ticos: �+�, �-� ,�*�, �/�.
� Operador de asignaci�n: �=�
� Comparadores: �>=�, �<=�, �>�, �<�, �==�, �<>�
� �(� �)��,� �:� y�.�
� Cadenas de caracteres correspondientes al tema particular de cada grupo.
� Palabras reservadas (en may�sculas):
IF, THEN, ELSE, END_IF, BEGIN, END, OUT
� y dem�s s�mbolos / tokens indicados en los temas particulares asignados al grupo.
El Analizador L�xico debe eliminar de la entrada (reconocer, pero no informarcomo tokens al Analizador
Sint�ctico), los siguientes elementos.
� Comentarios correspondientes al tema particular de cada grupo.
� Caracteres en blanco, tabulaciones y saltos de l�nea,que pueden aparecer en cualquier lugar de una
sentencia.
 */


/*agregados:
 * 2 6 10 13 16 18 20
 * 2. Enteros sin signo: Constantes enteras con valores entre 0 y 216 � 1.
 
 * 6. Dobles:N�meros reales con signo y parte exponencial. El exponente comienza con la letra E
(may�scula o min�scula) y puede tener signo. La ausencia de signo implica positivo. La parte
exponencial puede estar ausente. El s�mbolo decimal es la coma �,�.
Ejemplos v�lidos: 1, ,6 -1,2 3,E�5 2,e+34 2,5E1 13, 0,
Considerar rango2,2250738585072014E-308 <x < 1,7976931348623157E308(incluir el 0,0)
Se debe incorporar a la lista de palabras reservadas la palabra DOUBLE.
 
 *10. Incorporar a la lista de palabras reservadas las palabras SWITCH y CASE.
 
 *13. Incorporar a la lista de palabras reservadas las palabras FUNCTION, RETURN y MOVE.
 
 *16. Se definir� en el trabajo pr�ctico 2
 
 *18. Comentarios multil�nea: Comentarios que comiencen con �{ � y terminen con �}� (estos comentarios
pueden ocupar m�s de una l�nea).
 
 *20. Cadenas multil�nea: Cadenas de caracteres que comiencen con y terminen con � �� . Estas cadenas
pueden ocupar m�s de una l�nea, y en dicho caso, al final de cada l�nea, excepto la �ltima, deben
aparecer 3 puntos suspensivos� ... �. (En la Tabla de s�mbolos se guardar� la cadena sin los puntos
suspensivos, y sin el salto de l�nea.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Hashtable;

import tercetos.Elemento;
import compilador.AnalizadorLexico.RegistroTS;

public class Main {
	
	private static String levantarArchivo(String direccion){
			
			File file = new File(direccion);
			if (file.exists())
			{
			try
			{	
				String archivo = new String();
				String caracter;
				FileReader fr = new FileReader(file);
				BufferedReader br= new BufferedReader(fr);
				while ((caracter=br.readLine())!=null)
					archivo = archivo + caracter + '\n';
				br.close();
				return archivo;	
			}catch(IOException ex){ex.printStackTrace();}
			}
			return null;
		}
	private static void mostrarTS(Parser p,String nombre){
		try
		{
			PrintWriter pw = new PrintWriter(nombre+"_tabla_simbolos.txt");
			pw.println("TABLA DE SIMBOLOS:");
			pw.println("------------------");
			pw.println("");
			pw.printf("%-37s %-21s%21s","Lexema:","Tipo:","Uso:");
			pw.println("");
			pw.println("");
			Hashtable <String,RegistroTS>  tS = p.getAnalizador().getTS();
			Enumeration<String> palabras = tS.keys();
			while (palabras.hasMoreElements()){
				String key = palabras.nextElement();
				pw.printf("%-37s %-21s%21s",key,tS.get(key).tipo,tS.get(key).uso);
				pw.println("");
			}
			pw.close();
		}catch(IOException ex){ex.printStackTrace();}
		
	}
	private static void mostrarMensajes(Parser p,String nombre){
		try
		{
			File file = new File(nombre+"_mensajes.txt");
			Files.deleteIfExists(file.toPath());
			PrintWriter pw = new PrintWriter(file);
			pw.println("ERRORES DEL ANALIZADOR LEXICO:");
			System.out.println("ERRORES DEL ANALIZADOR LEXICO:");
			pw.println("------------------------------");
			System.out.println("------------------------------");
			pw.println("");
			if (p.getAnalizador().getErrores().isEmpty())
				pw.println("No se encontraron errores lexicos.");
			else
				for(String e:p.getAnalizador().getErrores())
				{
					System.out.println(e);
					pw.println(e);
				}
			pw.println("");
			pw.println("ERRORES SINTACTICOS:");
			System.out.println("ERRORES SINTACTICOS:");
			pw.println("--------------------");
			System.out.println("--------------------");
			pw.println("");
			if (p.getErrores().isEmpty())
				pw.println("No se detectaron errores sintacticos.");
			else
				for(String e: p.getErrores()){
				System.out.println(e);
				pw.println(e);
				}
			pw.println("");
			pw.println("ERRORES DE CHEQUEO SEMANTICO:");
			System.out.println("ERRORES DE CHEQUEO SEMANTICO:");
			pw.println("-----------------------------");
			System.out.println("-----------------------------");
			pw.println("");
			if (p.getErroresSemanticos().isEmpty())
				pw.println("No se detectaron errores de chequeo sem�ntico.");
			else
				{
				for(String e: p.getErroresSemanticos()){
					System.out.println(e);
					pw.println(e);
					}
				}
			
			pw.close();
		}catch(IOException ex){ex.printStackTrace();}
	}
	private static void mostrarTercetos(Parser p,String nombre){
		try
		{
			File file = new File(nombre+"_tercetos.txt");
			Files.deleteIfExists(file.toPath());
			PrintWriter pw = new PrintWriter(file);
			pw.println("----------------------------------------");
			pw.println("----- LISTA DE TERCETOS -----");
			pw.println("----------------------------------------");
			pw.println("");
			pw.println("Tercetos de las Funciones:");
			for (Elemento e: p.getAnalizador().getTercetosFuncImp())
				pw.println(e.getOutput());
			pw.println("");
			pw.println("Tercetos del Main:");
			for (Elemento e: p.getAnalizador().getTercetos())
				pw.println(e.getOutput());
			pw.println("");
			pw.close();
		}catch(IOException ex){ex.printStackTrace();}
		
	}
	private static void mostrarEstructuras(Parser p,String nombre){
		try
		{
			File file = new File(nombre+"_estructuras.txt");
			Files.deleteIfExists(file.toPath());
			PrintWriter pw = new PrintWriter(file);
			pw.println("TOKENS RECONOCIDOS POR EL ANALIZADOR LEXICO:");
			pw.println("--------------------------------------------");
			pw.println("");
			pw.printf("%-20s%s","Tipo:","Lexema:");
			pw.println("");
			pw.println("");
			for(String e:p.getAnalizador().getTokens())
					pw.println(e);
			pw.println("");
			pw.println("ESTRUCTURAS SINTACTICAS:");
			pw.println("------------------------");
			pw.println("");
			for(String e: p.getReglas())
					pw.println(e);
			pw.close();
		}catch(IOException ex){ex.printStackTrace();}
		
	}
	
	private static void generarCodigo(Parser p,String nombre,GeneradorAssembler g) throws IOException{
		try
		{
			File file = new File(nombre+".asm");
			Files.deleteIfExists(file.toPath());
			PrintWriter pw = new PrintWriter(file);
			pw.println(g.getAsm());
			pw.close();
		}catch(IOException ex){ex.printStackTrace();}		
	}
	
	private static String desecharExtension(String archivo){
		String nombre="";
		int i = archivo.lastIndexOf('.');
		if (i > 0) {
		    nombre = archivo.substring(0,i);
		}
		else nombre = archivo;
		return nombre; 
	}
	
	public static void main(String[] args) throws IOException {
		
		//Parser p = new Parser(levantarArchivo("Codigo.txt"));
		if (args.length==0){
			System.out.println("Error: nombre de archivo no especificado.");
			System.out.println("Uso:");
			System.out.println("     cgc 'nombre del archivo'");
			return;
	}
		String nombre=levantarArchivo(args[0]);
		if (nombre==null){
			System.out.println("Error: nombre de archivo invalido.");
			return;
		}
		Parser p = new Parser(nombre);
		//String nombre=levantarArchivo("Codigo2.txt");
		//Parser p = new Parser(nombre);
		p.run();
		GeneradorAssembler ga= new GeneradorAssembler(p);
		nombre=desecharExtension(args[0]);
		nombre=desecharExtension(nombre);
		mostrarTS(p,nombre);
		mostrarMensajes(p,nombre);
		//mostrarEstructuras(p,nombre);
		mostrarTercetos(p,nombre);
		generarCodigo(p,nombre,ga);
	}
}
