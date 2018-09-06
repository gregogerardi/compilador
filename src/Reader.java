import java.io.BufferedReader;
import java.io.FileReader;

public class Reader {
    private String sourceCode;
    private int position = 0;
    public static int actualLine = 1;
    BufferedReader inputReader;

    public int getActualLine() {
        return actualLine;
    }

    public boolean isNotFinal() {
        return position < sourceCode.length();
    }

    public boolean isFinal() {
        return position == sourceCode.length();
    }

    public Reader(String path) {
        BufferedReader inputReader = new BufferedReader(new FileReader(path));
        int read ;
        while (read=inputReader.read()!=-1){
        sourceCode+=read;}
    }
    public int getCaracter() {
        return sourceCode.charAt(position);
    }
    public void incPosition() {
        position++;
    }
    public void decPosition() {
        position--;
    }
}
