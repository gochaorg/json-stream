package xyz.cofe.grammar.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.bind.TermBind;

import java.lang.reflect.Type;

public record ValidatedParser(
    Type returnType,
    ImList<Param> inputPattern,
    StaticMethodParser resultMapper
) {
    public ImList<TermBind> termBindsOfParam(int index){
        return resultMapper.termBindOfParameter(index);
    }
}
