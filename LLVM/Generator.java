package LLVM;

import Syntax.Node.*;

import java.util.ArrayList;

public class Generator {

    public void generate(non_Terminal node) {
        if(node instanceof MainFuncDef) {generateMainFuncdef(node);}
        else if(node instanceof Block)  {generateBlock(node);}
        else if(node instanceof Stmt)   {generateStmt(node);}
        else {
            ArrayList<ASTNode> arrayList = node.getChild();
            for(ASTNode astNode : arrayList) {
                if(astNode instanceof Terminal)
                    continue;
                generate((non_Terminal) astNode);
            }
        }
    }
    public void generateCompUnit(non_Terminal node) {

    }

    public void generateMainFuncdef(non_Terminal node) {
        System.out.println("define i32 @main() {");
        generate((non_Terminal) node.getChild().get(4));
        System.out.println("}");
    }

    public void generateBlock(non_Terminal node) {
        generate((non_Terminal) node.getChild().get(1));
    }

    public void generateStmt(non_Terminal node) {
        ArrayList<ASTNode> arrayList = node.getChild();
        Terminal terminal = (Terminal) arrayList.get(0);
        if(terminal.getWord().getToken().equals("return")) {
            generate((non_Terminal) arrayList.get(1));
            System.out.println("ret i32 " + arrayList.get(1));
        }
    }
}
