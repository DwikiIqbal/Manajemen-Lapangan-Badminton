import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataMemberStore {
    private final File file;

    public DataMemberStore(String fileName) {
        this.file = new File(fileName);
    }

    public ArrayList<Member> load() {
        ArrayList<Member> members = new ArrayList<>();
        try {
            if (!file.exists()) {
                file.createNewFile();
                return members;
            }

            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] data = line.split(",");
                    if (data.length == 6) {
                        Member m = new Member(
                                data[1],
                                data[0],
                                data[2],
                                Integer.parseInt(data[3]),
                                Integer.parseInt(data[5])
                        );
                        m.setSisaSesi(Integer.parseInt(data[4]));
                        members.add(m);
                    } else if (data.length == 5) {
                        Member m = new Member(
                                data[1],
                                data[0],
                                data[2],
                                Integer.parseInt(data[3]),
                                1
                        );
                        m.setSisaSesi(Integer.parseInt(data[4]));
                        members.add(m);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("[ERROR] Gagal memuat data member: " + e.getMessage());
        }
        return members;
    }

    public void save(List<Member> members) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Member m : members) {
                writer.println(
                        m.getIdMember() + "," +
                        m.getNama() + "," +
                        m.getHariTetap() + "," +
                        m.getJamMulai() + "," +
                        m.getSisaSesi() + "," +
                        m.getNomorLapangan()
                );
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Gagal menyimpan data member: " + e.getMessage());
        }
    }
}
