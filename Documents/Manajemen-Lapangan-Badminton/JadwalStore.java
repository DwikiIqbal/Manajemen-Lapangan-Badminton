import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class JadwalStore {
    private final String filePath = "data_jadwal.txt";

    public void saveJadwal(ArrayList<Lapangan> daftarLapangan) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Lapangan lap : daftarLapangan) {
                for (Pelanggan p : lap.getJadwal()) {
                    String jenis = (p instanceof Member) ? "Member" : "Biasa";
                    int durasi = p.getJamSelesai() - p.getJamMulai();
                    boolean isBooking = false;
                    
                    if (p instanceof PelangganBiasa) {
                        isBooking = ((PelangganBiasa) p).getIsBooking();
                    }
                    
                    // Format TXT: NoLap,Nama,Jenis,JamMulai,Durasi,StatusBooking
                    writer.println(lap.getNomor() + "," +
                                   p.getNama() + "," +
                                   jenis + "," +
                                   p.getJamMulai() + "," +
                                   durasi + "," + 
                                   isBooking);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Gagal membackup jadwal lapangan: " + e.getMessage());
        }
    }

    public void loadJadwal(ArrayList<Lapangan> daftarLapangan) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    String[] data = line.split(",");
                    int noLap = Integer.parseInt(data[0]);
                    String nama = data[1];
                    String jenis = data[2];
                    int jamMulai = Integer.parseInt(data[3]);
                    int durasi = Integer.parseInt(data[4]);
                    boolean isBooking = Boolean.parseBoolean(data[5]);

                    for (Lapangan lap : daftarLapangan) {
                        if (lap.getNomor() == noLap) {
                            if (jenis.equalsIgnoreCase("Member")) {
                                // Bikin objek dummy buat ngisi slot jadwal di RAM
                                Member m = new Member(nama, "REF-"+nama, "Dinamis", jamMulai, noLap);
                                lap.tambahJadwal(m);
                            } else {
                                PelangganBiasa pb = new PelangganBiasa(nama, jamMulai, durasi, isBooking);
                                lap.tambahJadwal(pb);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[WARN] Jadwal corrupt diabaikan: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Gagal membaca file backup jadwal: " + e.getMessage());
        }
    }
}