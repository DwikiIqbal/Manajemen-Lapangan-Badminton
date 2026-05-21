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
                    
                    // Format TXT BARU: NoLap,Nama,Hari,Jenis,JamMulai,Durasi,StatusBooking
                    writer.println(lap.getNomor() + "," +
                                   p.getNama() + "," +
                                   p.getHariMain() + "," + // <-- Data Hari disimpan di sini
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
                    String hari = data[2]; // <-- Tangkap Data Hari di index 2
                    String jenis = data[3];
                    int jamMulai = Integer.parseInt(data[4]);
                    int durasi = Integer.parseInt(data[5]);
                    boolean isBooking = Boolean.parseBoolean(data[6]);

                    for (Lapangan lap : daftarLapangan) {
                        if (lap.getNomor() == noLap) {
                            if (jenis.equalsIgnoreCase("Member")) {
                                // Masukkan 'hari' ke objek Member
                                Member m = new Member(nama, "REF-"+nama, hari, jamMulai, noLap);
                                lap.tambahJadwal(m);
                            } else {
                                // Masukkan 'hari' ke objek PelangganBiasa
                                PelangganBiasa pb = new PelangganBiasa(nama, hari, jamMulai, durasi, isBooking);
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