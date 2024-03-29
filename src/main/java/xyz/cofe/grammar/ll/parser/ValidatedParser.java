package xyz.cofe.grammar.ll.parser;

import xyz.cofe.coll.im.ImList;
import xyz.cofe.grammar.ll.bind.TermBind;

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
