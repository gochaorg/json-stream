package xyz.cofe.json.stream.ast;

import xyz.cofe.json.stream.token.Colon;
import xyz.cofe.json.stream.token.Comma;
import xyz.cofe.json.stream.token.IndentTokenWriter;
import xyz.cofe.json.stream.token.SimpleTokenWriter;
import xyz.cofe.json.stream.token.TokenWriter;

import java.io.StringWriter;

/**
 * Записывает ({@link Ast}) дерево в {@link TokenWriter}
 */
public class AstWriter {
    /**
     * Преобразование к строке
     * @param ast дерево
     * @return строка
     */
    public static String toString(Ast<?> ast){
        if( ast==null ) throw new IllegalArgumentException("ast==null");
        StringWriter sw = new StringWriter();
        SimpleTokenWriter tokenWriter = new SimpleTokenWriter(sw);
        write(tokenWriter, ast);
        return sw.toString();
    }

    public static String toString(Ast<?> ast, boolean pretty){
        if( ast==null ) throw new IllegalArgumentException("ast==null");
        StringWriter sw = new StringWriter();
        SimpleTokenWriter tokenWriter = new SimpleTokenWriter(sw);

        if( pretty ) {
            IndentTokenWriter iTW = new IndentTokenWriter(tokenWriter);
            write(iTW, ast);
        }else{
            write(tokenWriter, ast);
        }

        return sw.toString();
    }

    public static void write(TokenWriter writer, Ast<?> ast) {
        if (writer == null) throw new IllegalArgumentException("writer==null");
        if (ast == null) throw new IllegalArgumentException("ast==null");

        if (ast instanceof Ast.IdentAst<?> id) {
            writer.write(id.token());
        } else if (ast instanceof Ast.NumberAst.BigIntAst<?> num) {
            writer.write(num.token());
        } else if (ast instanceof Ast.NumberAst.DoubleAst<?> num) {
            writer.write(num.token());
        } else if (ast instanceof Ast.NumberAst.LongAst<?> num) {
            writer.write(num.token());
        } else if (ast instanceof Ast.NumberAst.IntAst<?> num) {
            writer.write(num.token());
        } else if (ast instanceof Ast.Comment.SingleLine<?> cmt) {
            writer.write(cmt.token());
        } else if (ast instanceof Ast.Comment.MultiLine<?> cmt) {
            writer.write(cmt.token());
        } else if (ast instanceof Ast.BooleanAst.TrueAst<?> value) {
            writer.write(value.token());
        } else if (ast instanceof Ast.BooleanAst.FalseAst<?> value) {
            writer.write(value.token());
        } else if (ast instanceof Ast.NullAst<?> value) {
            writer.write(value.token());
        } else if (ast instanceof Ast.StringAst<?> value) {
            writer.write(value.token());
        } else if (ast instanceof Ast.ArrayAst<?> arr) {
            writer.write(arr.begin());
            Ast<?> prev = null;
            for (var item : arr.values()) {
                if (prev != null) {
                    writer.write(Comma.instance);
                }
                write(writer, item);
                prev = item;
            }
            writer.write(arr.end());
        } else if (ast instanceof Ast.ObjectAst<?> obj) {
            writer.write(obj.begin());

            Ast.KeyValue<?> prev = null;
            for (var kv : obj.values()) {
                if( prev!=null ){
                    writer.write(Comma.instance);
                }
                prev = kv;

                Ast<?> key = kv.key();
                var value = kv.value();

                write(writer, key);
                writer.write(Colon.instance);
                write(writer, value);
            }
            writer.write(obj.end());
        }
    }
}
