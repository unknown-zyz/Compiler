package Frontend.Lexical;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static Config.Config.TokenPath;

public class Lexer {
    private final ArrayList<Word> TokenList = new ArrayList<>();

    public static final HashMap<String, TokenType> reserved = new HashMap<>(){{
        put("main", TokenType.MAINTK);
        put("const", TokenType.CONSTTK);
        put("int", TokenType.INTTK);
        put("void", TokenType.VOIDTK);
        put("break", TokenType.BREAKTK);
        put("continue", TokenType.CONTINUETK);
        put("if", TokenType.IFTK);
        put("else", TokenType.ELSETK);
        put("for", TokenType.FORTK);
        put("getint", TokenType.GETINTTK);
        put("printf", TokenType.PRINTFTK);
        put("return", TokenType.RETURNTK);
    }};

    public static final HashMap<String, TokenType> symbols = new HashMap<>(){{
        put("!", TokenType.NOT);
        put("&&", TokenType.AND);
        put("||", TokenType.OR);
        put("+", TokenType.PLUS);
        put("-", TokenType.MINU);
        put("*", TokenType.MULT);
        put("/", TokenType.DIV);
        put("%", TokenType.MOD);
        put("<", TokenType.LSS);
        put("<=", TokenType.LEQ);
        put(">", TokenType.GRE);
        put(">=", TokenType.GEQ);
        put("==", TokenType.EQL);
        put("!=", TokenType.NEQ);
        put("=", TokenType.ASSIGN);
        put(";", TokenType.SEMICN);
        put(",", TokenType.COMMA);
        put("(", TokenType.LPARENT);
        put(")", TokenType.RPARENT);
        put("[", TokenType.LBRACK);
        put("]", TokenType.RBRACK);
        put("{", TokenType.LBRACE);
        put("}", TokenType.RBRACE);
    }};
    public void analyse(String str) {
        int pos = 0;
        int line = 1;
        int len = str.length();
        while (pos < len) {
            char cur =  str.charAt(pos);
//            char cur_next = str.charAt(pos + 1);
            if(Character.isWhitespace(cur)) {
                if(cur == '\n') {
                    line++;
                }
                pos++;
            }
            else if(Character.isLetter(cur) || cur == '_') {
                int start = pos;
                while(pos < len && (Character.isLetter(str.charAt(pos)) || Character.isDigit(str.charAt(pos)) || str.charAt(pos) == '_')) {
                    pos++;
                }
                int end = pos;
                String token = str.substring(start, end);
                TokenType tokenType;
                if(reserved.containsKey(token))
                    tokenType = reserved.get(token);
                else
                    tokenType = TokenType.IDENFR;
                Word word = new Word(token, tokenType, line);
                TokenList.add(word);
            }
            else if(Character.isDigit(cur)) {
                int start = pos;
                while(pos < len && Character.isDigit(str.charAt(pos))) {
                    pos++;
                }
                int end = pos;
                String token = str.substring(start, end);
                Word word = new Word(token, TokenType.INTCON, line);
//                if(!Frontend.Syntax.Node.non_Terminal.isFormatString(word))
//                    System.out.println("error a"+word.getLine());
                TokenList.add(word);
            }
            else if(cur == '\"') {
                int start = pos;
                pos++;
                while(pos < len && str.charAt(pos) != '\"') {
                    pos++;
                }
                int end = pos;
                String token = str.substring(start, end + 1);
                Word word = new Word(token, TokenType.STRCON, line);
                TokenList.add(word);
                pos++;
            }
            else if(cur == '!' || cur == '<' || cur == '>' || cur == '=') {
                if(pos + 1 < len && str.charAt(pos + 1) == '=') {
                    String token = str.substring(pos, pos + 2);
                    Word word = new Word(token, symbols.get(token), line);
                    TokenList.add(word);
                    pos += 2;
                }
                else {
                    String token = str.substring(pos, pos + 1);
                    Word word = new Word(token, symbols.get(token), line);
                    TokenList.add(word);
                    pos++;
                }
            }
            else if(cur == '&') {
                if(pos + 1 < len && str.charAt(pos + 1) == '&') {
                    String token = str.substring(pos, pos + 2);
                    Word word = new Word(token, symbols.get(token), line);
                    TokenList.add(word);
                    pos += 2;
                }
                else {
                    System.out.println("Frontend.Semantic.Error: line " + line + ": " + cur + " is not a valid character");
                    pos++;
                }
            }
            else if(cur == '|') {
                if(pos + 1 < len && str.charAt(pos + 1) == '|') {
                    String token = str.substring(pos, pos + 2);
                    Word word = new Word(token, symbols.get(token), line);
                    TokenList.add(word);
                    pos += 2;
                }
                else {
                    System.out.println("Frontend.Semantic.Error: line " + line + ": " + cur + " is not a valid character");
                    pos++;
                }
            }
            else if(cur == '/') {
                if(pos + 1 < len && str.charAt(pos + 1) == '/') {
                    while(pos < len && str.charAt(pos) != '\n') {
                        pos++;
                    }
                    line++;
                    pos++;
                }
                else if(pos + 1 < len && str.charAt(pos + 1) == '*') {
                    while(pos < len && !(str.charAt(pos) == '*' && str.charAt(pos + 1) == '/')) {
                        if(str.charAt(pos) == '\n') {
                            line++;
                        }
                        pos++;
                    }
                    pos += 2;
                }
                else {
                    String token = str.substring(pos, pos + 1);
                    Word word = new Word(token, symbols.get(token), line);
                    TokenList.add(word);
                    pos++;
                }
            }
            else {
                String token = str.substring(pos, pos + 1);
                Word word = new Word(token, symbols.get(token), line);
                TokenList.add(word);
                pos++;
            }
        }
    }

    public ArrayList<Word> getTokenList() {
        return TokenList;
    }

    public void print() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(TokenPath);
        for (Word word : TokenList) {
            writer.println(word.getType() + " " + word.getToken());
        }
        writer.close();
    }
}
