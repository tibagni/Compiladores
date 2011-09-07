package comp.app;

import java.util.HashMap;

public class Simbolos {
    public static final int SINDEFINIDO       = -1;
    public static final int SPROGRAMA         = 0;
    public static final int SINICIO           = 1;
    public static final int SFIM              = 2;
    public static final int SPROCEDIMENTO     = 3;
    public static final int SFUNCAO           = 4;
    public static final int SSE               = 5;
    public static final int SENTAO            = 6;
    public static final int SSENAO            = 7;
    public static final int SENQUANTO         = 8;
    public static final int SFACA             = 9;
    public static final int SATRIBUICAO       = 10;
    public static final int SESCREVA          = 11;
    public static final int SLEIA             = 12;
    public static final int SVAR              = 13;
    public static final int SINTEIRO          = 14;
    public static final int SBOOLEANO         = 15;
    public static final int SIDENTIFICADOR    = 16;
    public static final int SNUMERO           = 17;
    public static final int SPONTO            = 18;
    public static final int SPONTO_VIRGULA    = 19;
    public static final int SVIRGULA          = 20;
    public static final int SABRE_PARENTESES  = 21;
    public static final int SFECHA_PARENTESES = 22;
    public static final int SMAIOR            = 23;
    public static final int SMAIORIG          = 24;
    public static final int SIG               = 25;
    public static final int SMENOR            = 26;
    public static final int SMENORIG          = 27;
    public static final int SDIF              = 28;
    public static final int SMAIS             = 29;
    public static final int SMENOS            = 30;
    public static final int SMULT             = 31;
    public static final int SDIV              = 32;
    public static final int SE                = 33;
    public static final int SOU               = 34;
    public static final int SNAO              = 35;
    public static final int SDOISPONTOS       = 36;
    public static final int SVERDADEIRO       = 37;
    public static final int SFALSO            = 38;

    public static final HashMap<String, Integer> PALAVRAS_RESERVADAS = new HashMap<String, Integer>();

    static {
        PALAVRAS_RESERVADAS.put("programa", SPROGRAMA);
        PALAVRAS_RESERVADAS.put("se", SSE);
        PALAVRAS_RESERVADAS.put("entao", SENTAO);
        PALAVRAS_RESERVADAS.put("senao", SSENAO);
        PALAVRAS_RESERVADAS.put("enquanto", SENQUANTO);
        PALAVRAS_RESERVADAS.put("faca", SFACA);
        PALAVRAS_RESERVADAS.put("inicio", SINICIO);
        PALAVRAS_RESERVADAS.put("fim", SFIM);
        PALAVRAS_RESERVADAS.put("escreva", SESCREVA);
        PALAVRAS_RESERVADAS.put("leia", SLEIA);
        PALAVRAS_RESERVADAS.put("var", SVAR);
        PALAVRAS_RESERVADAS.put("inteiro", SINTEIRO);
        PALAVRAS_RESERVADAS.put("booleano", SBOOLEANO);
        PALAVRAS_RESERVADAS.put("verdadeiro", SVERDADEIRO);
        PALAVRAS_RESERVADAS.put("falso", SFALSO);
        PALAVRAS_RESERVADAS.put("procedimento", SPROCEDIMENTO);
        PALAVRAS_RESERVADAS.put("funcao", SFUNCAO);
        PALAVRAS_RESERVADAS.put("div", SDIV);
        PALAVRAS_RESERVADAS.put("e", SE);
        PALAVRAS_RESERVADAS.put("ou", SOU);
        PALAVRAS_RESERVADAS.put("nao", SNAO);
    }

    public static String getSymbolDescription(int symbol) {
        switch(symbol) {
            case SPROGRAMA: return "sPrograma";
            case SINICIO: return "sInicio";
            case SFIM: return "sFim";
            case SPROCEDIMENTO: return "sProcedimento";
            case SFUNCAO: return "sFuncao";
            case SSE: return "sSe";
            case SENTAO: return "sEntao";
            case SSENAO: return "sSenao";
            case SENQUANTO: return "sEnquanto";
            case SFACA: return "sFaca";
            case SATRIBUICAO: return "sAtribuicao";
            case SESCREVA: return "sEscreva";
            case SLEIA: return "sLeia";
            case SVAR: return "sVar";
            case SINTEIRO: return "sInteiro";
            case SBOOLEANO: return "sBoolaeno";
            case SIDENTIFICADOR: return "sIdentificador";
            case SNUMERO: return "sNumero";
            case SPONTO: return "sPonto";
            case SPONTO_VIRGULA: return "sPonto_vitgula";
            case SVIRGULA: return "sVirgula";
            case SABRE_PARENTESES: return "sAbre_parenteses";
            case SFECHA_PARENTESES: return "sFecha_parenteses";
            case SMAIOR: return "sMaior";
            case SMAIORIG: return "sMaiorig";
            case SIG: return "sIg";
            case SMENOR: return "sMenor";
            case SMENORIG: return "sMenorig";
            case SDIF: return "sDif";
            case SMAIS: return "sMais";
            case SMENOS: return "sMenos";
            case SMULT: return "sMult";
            case SDIV: return "sDiv";
            case SE: return "sE";
            case SOU: return "sOu";
            case SNAO: return "sNao";
            case SDOISPONTOS: return "sDoispontos";
            case SVERDADEIRO: return "sVerdadeiro";
            case SFALSO: return "sFalso";
            default: return "Símbolo indefinido";
        }
    }
}
