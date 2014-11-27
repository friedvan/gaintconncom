import java.io.*;
import java.util.*;

/**
 * Created by pzc on 11/27/14.
 */
public class GaintComponent {

    private Map<Integer, Integer> status;
    private Map<Integer, Map<Integer, String>> network;
    private Map<Integer, Map<Integer, String>> networkReverse;

    private String networkFile;
    private int maxsize;
    private int maxid;


    public GaintComponent(String networkFile) {
        this.networkFile = networkFile;
        this.maxsize = 0;
        this.maxid = 0;
        this.status = new HashMap<Integer, Integer>();
        this.network = new HashMap<Integer, Map<Integer, String>>();
        this.networkReverse = new HashMap<Integer, Map<Integer, String>>();
    }

    public void readNetwork() throws IOException {
        FileReader fr = new FileReader(this.networkFile);
        BufferedReader br = new BufferedReader(fr);

        String line = "";
        String[] cols;
        int count = 0;
        while ((line = br.readLine()) != null) {
            cols = line.split("\t");
            int uidFrom = Integer.valueOf(cols[0]);
            int uidTo = Integer.valueOf(cols[1]);
            String value = cols[2] + "\t" + cols[3];
            if (!network.containsKey(uidFrom)) {
                network.put(uidFrom, new HashMap<Integer, String>());
            }
            network.get(uidFrom).put(uidTo, value);
            count ++;
            if (!networkReverse.containsKey(uidTo)) {
                networkReverse.put(uidTo, new HashMap<Integer, String>());
            }
            networkReverse.get(uidTo).put(uidFrom, value);
        }
        System.out.println(count);
    }

    private int component(int from, int id) {
        int size = 0;
        Stack<Integer> uidStack = new Stack<Integer>();
        uidStack.push(from);
        while (!uidStack.empty()) {
            size ++;
            Integer uid = uidStack.pop();
            status.put(uid, id);
            Map<Integer, String> neighbors = (network.get(uid) == null) ? new HashMap<Integer, String>() : network.get(uid);
            Map<Integer, String> neighborsReverse = (networkReverse.get(uid) == null) ? new HashMap<Integer, String>() : networkReverse.get(uid);
            for (Map.Entry<Integer, String> entry : neighbors.entrySet()) {
                int neighbor = entry.getKey();
                if (!status.containsKey(neighbor)) {
                    uidStack.push(neighbor);
                }
            }
            for (Map.Entry<Integer, String> entry : neighborsReverse.entrySet()) {
                int neighbor = entry.getKey();
                if (!status.containsKey(neighbor)) {
                    uidStack.push(neighbor);
                }
            }

        }
        return size;
    }

    public void calcGaintComponent() {
        int id = 1;
        for (Map.Entry<Integer, Map<Integer, String>> mapEntry : network.entrySet()) {
            Integer from = mapEntry.getKey();
            if (status.containsKey(from)) {
                continue;
            }
            int size = component(from, id);
            if (size > maxsize) {
                maxsize = size;
                maxid = id;
            }
//            System.out.println(id + " " + size);
            id++;
        }
    }

    public void saveGaintComponent(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        BufferedWriter bw = new BufferedWriter(fw);
        for (Map.Entry<Integer, Map<Integer, String>> entry : network.entrySet()) {
            int from = entry.getKey();
            int id = status.get(from);
            if (id != maxid) {
                continue;
            }
            Map<Integer, String> neighbors = entry.getValue();
            for (Map.Entry<Integer, String> neighbor : neighbors.entrySet()) {
                String out = from + "\t" + neighbor.getKey() + "\t" + neighbor.getValue();
                bw.write(out + '\n');
            }
        }

        bw.close();
        fw.close();
    }

    public void saveComponentId(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        BufferedWriter bw = new BufferedWriter(fw);
        int count = 0;
        for (Map.Entry<Integer, Map<Integer, String>> entry : network.entrySet()) {
            int from = entry.getKey();
            int id = status.get(from);

            Map<Integer, String> neighbors = entry.getValue();
            for (Map.Entry<Integer, String> neighbor : neighbors.entrySet()) {
                String out = from + "\t" + neighbor.getKey() + "\t" + neighbor.getValue() + "\t" + id;
                bw.write(out + '\n');
                count ++;
            }
        }
        System.out.println(count);
        bw.close();
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        GaintComponent gc = new GaintComponent("social_network_200w.txt");
        gc.readNetwork();
        gc.calcGaintComponent();
        gc.saveGaintComponent("social_network_200w_gaint.txt");
        gc.saveComponentId("social_network_200w_label.txt");
    }
}
