package xyz.cofe.json.stream.token;

import java.io.IOException;

public class SimpleTokenWriter implements TokenWriter {
    private final Appendable output;

    public SimpleTokenWriter(Appendable output){
        if( output==null ) throw new IllegalArgumentException("output==null");
        this.output = output;
    }

    @Override
    public void write(BigIntToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(token.value().toString() + "n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(LongToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(Long.toString(token.value()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(IntToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(Integer.toString(token.value()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(DoubleToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(Double.toString(token.value()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(StringToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            String str = token.value();
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            for( var i=0;i<str.length();i++ ){
                var chr = str.charAt(i);
                int ichr = chr;
                if( chr=='\n' ){
                    sb.append("\\n");
                }else if( chr=='\r' ){
                    sb.append("\\r");
                }else if( chr=='\"'){
                    sb.append("\\\"");
                }else if( chr=='\t' ){
                    sb.append("\\t");
                }else if( ichr<32 ){
                    var hex = Integer.toHexString(ichr);
                    if( hex.length()==1 )hex = "0"+hex;
                    sb.append("\\x").append(hex);
                }else{
                    sb.append(chr);
                }
            }
            sb.append("\"");
            output.append(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(FalseToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("false");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(TrueToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("true");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(NullToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("null");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(IdentifierToken<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(token.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(OpenParentheses<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("{");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(CloseParentheses<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(OpenSquare<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("[");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(CloseSquare<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append("]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Colon<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(":");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Comma<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(",");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(MLComment<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(token.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(SLComment<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(token.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Whitespace<?> token) {
        if( token==null ) throw new IllegalArgumentException("token==null");
        try {
            output.append(" ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
