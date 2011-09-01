package comp.app;

public class Token {
    private String mLexema;
    private int mSimbolo;

    public Token(String l, int s) {
        mLexema = l;
        mSimbolo = s;
    }

    /**
     * @return the lexema
     */
    public String getLexema() {
        return mLexema;
    }

    /**
     * @return the simbolo
     */
    public int getSimbolo() {
        return mSimbolo;
    }

    /**
     * @param lexema the lexema to set
     */
    public void setLexema(String lexema) {
        mLexema = lexema;
    }

    /**
     * @param simbolo the simbolo to set
     */
    public void setSimbolo(int simbolo) {
        mSimbolo = simbolo;
    }
    
    //TODO Apagar depois hahaha
    public String toString() {
    	return "Lexema: " + mLexema + "  Simbolo: " + mSimbolo;
    }
}
