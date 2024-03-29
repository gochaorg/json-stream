package xyz.cofe.grammar.ll;

import xyz.cofe.coll.im.ImList;

import java.util.Optional;

public interface Pointer<T> {
    boolean eof();
    Optional<T> get();
    Pointer<T> move(int offset);

    class ImListPointer<T> implements Pointer<T> {
        public final ImList<T> list;
        public final int index;

        public ImListPointer(ImList<T> list){
            if( list==null ) throw new IllegalArgumentException("list==null");
            this.list = list;
            this.index = 0;
        }

        public ImListPointer(ImList<T> list, int index){
            if( list==null ) throw new IllegalArgumentException("list==null");
            this.list = list;
            this.index = index;
        }

        @Override
        public boolean eof() {
            return index < 0 || index >= list.size();
        }

        @Override
        public Optional<T> get() {
            return list.get(index);
        }

        @Override
        public ImListPointer<T> move(int offset) {
            return new ImListPointer<T>(list, index+offset);
        }
    }
}
