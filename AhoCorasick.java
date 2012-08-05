/*
 * AhoCorasick.java
 * Created on 2010/12/03
 * @package
 * @author	Hiroki Tanioka
 * Aho Corasick Algorithm
 * @update	2010/12/03 Hiroki Tanioka	1st edition
 * @reference	http://www.prefield.com/algorithm/string/aho_corasick.html
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AhoCorasick {

    private class PMA {
        PMA[] next = new PMA[0x100]; // next[0] is for fail
        List<Integer> accept = new ArrayList<Integer>();
    }

    private volatile PMA root;
    private List<PMA> list = new ArrayList<PMA>();
    private List<String> words = new ArrayList<String>();

    private PMA newPMA() {
        PMA p = null;
        try {
            p = new PMA();
        } catch (Exception e) {
            e.printStackTrace();
            p = null;
            return null;
        }
        list.add(p);
        return p;
    }

    /**
     * constructor
     */
    public AhoCorasick() {
        root = null;
    }

    public boolean isBuilt() {
        return root != null;
    }

    public synchronized PMA build(List<byte[]> p) {
        final int size = p.size();
        PMA _root = newPMA();
        root = _root;
        for (int i = 0; i < size; ++i) {
            byte[] _p = p.get(i);
            words.add(new String(_p));
            // make trie
            PMA t = root;
            final int _size = _p.length;
            for (int j = 0; j < _size; ++j) {
                int c = (int) (_p[j] & 0xff);
                if (t.next[c] == null) {
                    t.next[c] = newPMA();
                }
                t = t.next[c];
            }
            t.accept.add(i);
        }
        Queue<PMA> q = new LinkedList<PMA>(); // make failure link using bfs
        for (int c = 0x01; c <= 0xff; ++c) {
            if (root.next[c] != null) {
                root.next[c].next[0] = root;
                q.add(root.next[c]);
            } else {
                root.next[c] = root;
            }
        }
        while (q.size() > 0) {
            PMA t = q.poll();
            for (int c = 0x01; c <= 0xff; ++c) {
                if (t.next[c] != null) {
                    q.add(t.next[c]);
                    PMA r = t.next[0];
                    while (r.next[c] == null) {
                        r = r.next[0];
                    }
                    t.next[c].next[0] = r.next[c];
                }
            }
        }

        return root;
    }

    public synchronized String match(final byte[] t) {
        String result = "";
        PMA v = root;
        if (v == null) {
            System.err.println("wrong root object in match.");
            return null;
        }
        int n = t.length;
        PMA _root = v;
        for (int i = 0; i < n; ++i) {
            int c = (int) (t[i] & 0xff);
            if (c < 0 || c > 0xff) {
                c = 0xff;
                System.err.println("There is an irregular character in URL("
                        + t + ").");
            }
            if (v == null) {
                v = root;
            }
            while (v.next[c] == null) {
                v = v.next[0];
                if (v == null) {
                    break;
                }
            }
            if (v == null) {
                continue;
            }
            v = v.next[c];
            if (result != null) {
                if (v.accept.size() > 0) {
                    result = words.get(v.accept.get(0));
                    return result;
                }
            } else if (v.accept.size() > 0) {
                return result;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        AhoCorasick ac = new AhoCorasick();

        System.out.println("build ...");
        List<byte[]> list = new ArrayList<byte[]>();
        for (int i = 0; i < args.length - 1; i++) {
            list.add(args[i].getBytes());
            System.out.println(args[i]);
        }
        ac.build(list);

        String test = args[args.length - 1];
        System.out.println("");
        System.out.print("search " + test);

        String result = ac.match(test.getBytes());

        if (result == null || "".equals(result)) {
            System.out.println(" ... none");
        } else {
            System.out.println(" ... hit!! ");
        }
    }
}
