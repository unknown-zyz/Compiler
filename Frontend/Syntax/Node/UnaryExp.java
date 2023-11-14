package Frontend.Syntax.Node;

import static Frontend.Syntax.SyntaxMain.*;

import Frontend.Semantic.Error.ErrorType;
import Frontend.Semantic.Symbols.ArraySymbol;
import Frontend.Semantic.Symbols.FuncSymbol;
import Frontend.Semantic.Symbols.Functype;

import java.util.ArrayList;

public class UnaryExp extends non_Terminal {
    @Override
    public void analyse() {
        if(cur_equal("+") || cur_equal("-") || cur_equal("!")) {
            add_analyse(new UnaryOp());
            add_analyse(new UnaryExp());
        }
        else if(cur_equal("(") || isIntConst(cur)) {
            add_analyse(new PrimaryExp());
        }
        else if(isIdent(cur))
        {
            if(getNext().getToken().equals("("))
            {
                if(!queryGlobalSymbol(cur.getToken()))
                    addError(ErrorType.c, cur.getLine());
                callFlag = true;
                String name = cur.getToken();
                addChild(new Ident(cur));
                next();
                addChild(new Symbol(cur));
                next();

//                System.out.println(name);
                int FParams_cnt = getFParamCnt(name);
                int RParams_cnt = getRParamCnt(getIndex()-1);
//                System.out.println("Fcnt="+FParams_cnt);
//                System.out.println("Rcnt="+RParams_cnt);
                if(RParams_cnt != FParams_cnt && FParams_cnt != -1 )
                {
                    addError(ErrorType.d);
                }


                ArrayList<Integer> FParams = getFParamDimension(name);
//                for(Integer i: FParams)
//                    System.out.println("F"+i);
//                System.out.println(getword(getIndex()).getToken());
                ArrayList<Integer> RParams = getRParamDimension(getIndex()-1);
//                System.out.println("RParams size"+RParams.size());
//                for(Integer i: RParams)
//                    System.out.println("R"+i);
                if(RParams.size() == FParams_cnt)
                {
                    for(int i=0;i<FParams_cnt;i++)
                    {
                        if(!RParams.get(i).equals(FParams.get(i)))
                        {
//                            System.out.println("F"+FParams.get(i));
//                            System.out.println("R"+RParams.get(i));
                            addError(ErrorType.e);
                        }

                    }
                }

                if(cur_equal("(")||cur_equal("+")||cur_equal("-")||cur_equal("!")||isIdent(cur)|| isIntConst(cur))
                    add_analyse(new FuncRParams());
                if(cur_equal(")")) {
                    addChild(new Symbol(cur));
                    next();
                }
                else
                    addError(ErrorType.j);
                callFlag = false;
            }
            else {
                add_analyse(new PrimaryExp());
            }
        }
    }

    public int getRParamCnt(int index) {
        int line = getword(index).getLine();
        if(getword(index).getToken().equals(")") || getword(index).getToken().equals(";"))  return 0;
        int cnt=1, lcnt=1;
        while(!getword(index).getToken().equals(";") && getword(index).getLine()==line && lcnt>0)
        {
            if(getword(index).getToken().equals(",") && lcnt==1)
                cnt++;
            else if(getword(index).getToken().equals("("))
                lcnt++;
            else if(getword(index).getToken().equals(")"))
                lcnt--;
            index++;
        }
        return cnt;
    }
    public ArrayList<Integer> getRParamDimension(int index) {
        ArrayList<Integer> ret = new ArrayList<>();
        int cnt = 1;    //左括号数量
        int line = getword(index).getLine();
        while(cnt > 0 && getword(index).getLine()==line)
        {
//            System.out.println(getword(index).getToken());
            if(isIdent(getword(index)))
            {
                Frontend.Semantic.Symbols.Symbol sym = getSymbol(getword(index).getToken());
                if(sym instanceof ArraySymbol)
                {
                    int dim = ((ArraySymbol) sym).getDimension();
                    index++;
                    while(!getword(index).getToken().equals(",") && !getword(index).getToken().equals(")") && !getword(index).getToken().equals(";") && !isIdent(getword(index)))
                    {
                        if(getword(index).getToken().equals("["))
                            dim--;
                        index++;
                    }
                    ret.add(dim);
                }
                else if(sym instanceof FuncSymbol)
                {
                    if(((FuncSymbol) sym).getFunctype()== Functype.INT)
                        ret.add(0);
                    else if(((FuncSymbol) sym).getFunctype()== Functype.VOID)
                        ret.add(-1);
                    index++;
                }
            }
            else if(isIntConst(getword(index)))
            {
                ret.add(0);
                index++;
            }
            while(!getword(index).getToken().equals(",") && cnt>0 && getword(index).getLine()==line)
            {
                if(getword(index).getToken().equals(";"))
                {
                    cnt=-1;
                    break;
                }
                if(getword(index).getToken().equals("("))
                    cnt++;
                if(getword(index).getToken().equals(")"))
                    cnt--;
                index++;
            }
            index++;
        }
        return ret;
    }
}
