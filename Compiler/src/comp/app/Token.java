package comp.app;

public class Token {
    private String mLexema;
    private int mSimbolo;
    private int mLine; // Linha do token
    private int mCol;  // Coluna que termina o token

    public Token(String l, int s) {
        this(l, s, 0, 0);
    }

    public Token(String l, int s, int li, int c) {
        mLexema = l;
        mSimbolo = s;
        mLine = li;
        mCol = c;
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
    public int getSymbol() {
        return mSimbolo;
    }

    /**
     * 
     * @return Linha do token
     */
    public int getTokenLine() {
        return mLine;
    }

    /**
     * 
     * @return Coluna do fim do token
     */
    public int getTokenEndColumn() {
        return mCol;
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

    /**
     * 
     * @param line Linha do token
     */
    public void setTokenLine(int line) {
        mLine = line;
    }

    /**
     * 
     * @param column Coluna do fim do token
     */
    public void setTokenEndColumn(int column) {
        mCol = column;
    }

    public String toString() {
    	return "[" + mLexema + "] => {" + Symbols.getSymbolDescription(mSimbolo) + "}" + " - " + mLine + " : " + mCol;
    }
}
