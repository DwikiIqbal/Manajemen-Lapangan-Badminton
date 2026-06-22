import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SistemGOR {
    private static final int JAM_BUKA = 7;
    private static final int JAM_TUTUP = 22;
    private static final String FILE_MEMBER = "data_member.txt";

    private final Scanner scanner = new Scanner(System.in);
    private final ArrayList<Lapangan> daftarLapangan = new ArrayList<>();
    private final ArrayList<Member> databaseMember = new ArrayList<>();
    private final ArrayList<Barang> daftarBarang = new ArrayList<>();
    private final ArrayList<Transaksi> riwayatTransaksi = new ArrayList<>();
    private final Set<String> memberCheckedInHariIni = new HashSet<>();
    
    // Inisialisasi Store File I/O
    private final DataMemberStore memberStore = new DataMemberStore(FILE_MEMBER);
    private final JadwalStore jadwalStore = new JadwalStore(); 
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String tanggalCheckInAktif = "";

    private static class PelangganAktif {
        private final String nama;
        private final int nomorLapangan;

        private PelangganAktif(String nama, int nomorLapangan) {
            this.nama = nama;
            this.nomorLapangan = nomorLapangan;
        }

        private String label() {
            return nama + " (Lapangan " + nomorLapangan + ")";
        }
    }

    public void run() {
        inisialisasiData();
        boolean isRunning = true;

        while (isRunning) {
            clearScreen();
            tampilkanHeader();

            System.out.print("\nPilih menu (1-7): ");
            int pilihan = inputInt();

            switch (pilihan) {
                case 1: menuCekLapangan(); break;
                case 2: menuTransaksiBiasa(); break;
                case 3: menuMember(); break;
                case 4: menuToko(); break;
                case 5: menuLaporanKeuangan(); break;
                case 6: menuPelunasan(); break;
                case 7:
                    System.out.println("\nMakasih udah pake GOR Silma!");
                    System.out.println("Data Member & Jadwal udah ke-save otomatis.");
                    isRunning = false;
                    break;
                default:
                    System.out.println("\nPilihan gak valid. Pilih 1-7.");
            }

            // tanpa pause, langsung loop
        }

        memberStore.save(databaseMember);
    }

    private void inisialisasiData() {
        databaseMember.addAll(memberStore.load());
        tanggalCheckInAktif = tanggalHariIni();

        for (int i = 1; i <= 8; i++) {
            daftarLapangan.add(new Lapangan(i));
        }
        
        jadwalStore.loadJadwal(daftarLapangan);

        daftarBarang.add(new Barang("Shuttlecock", 10000, 50));
        daftarBarang.add(new Barang("Air Mineral", 5000, 100));
        daftarBarang.add(new Barang("Sewa Sepatu", 20000, 10));
    }

    // Mengambil hari secara otomatis dari sistem operasi (Fitur Anti Time-Freeze)
    private String getHariIni() {
        return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
    }

    private void tampilkanHeader() {
        System.out.println("+=======================================+");
        System.out.println("|   GOR SILMA BADMINTON - Bekasi        |");
        System.out.println("|   Hari: " + getHariIni());
        System.out.println("+=======================================+");
        System.out.println("| 1. Cek Lapangan                       |");
        System.out.println("| 2. Sewa / Booking (Pelanggan Biasa)   |");
        System.out.println("| 3. Member                             |");
        System.out.println("| 4. Toko Perlengkapan                  |");
        System.out.println("| 5. Laporan Keuangan                   |");
        System.out.println("| 6. Pelunasan Booking                  |");
        System.out.println("| 7. Keluar                             |");
        System.out.println("+=======================================+");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println();
    }

    private int inputJam(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            
            String normalized = input.replace(',', '.').replace(':', '.');
            
            if (normalized.equals("0")) return 0;
            
            try {
                if (normalized.contains(".")) {
                    String[] parts = normalized.split("\\.");
                    return Integer.parseInt(parts[0]);
                } else {
                    int jam = Integer.parseInt(normalized);
                    if (jam >= 0 && jam <= 23) return jam;
                    System.out.println("  (Jam 0-23 aja ya)");
                }
            } catch (NumberFormatException e) {
                System.out.println("  (Format salah, contoh: 13.00 atau 13)");
            }
        }
    }

    private int inputInt() {
        while (!scanner.hasNextInt()) {
            System.out.print("Input tidak valid! Harus berupa angka: ");
            scanner.next();
        }
        int hasil = scanner.nextInt();
        scanner.nextLine();
        return hasil;
    }

    private String tanggalHariIni() {
        return dateFormat.format(new Date());
    }

    private void refreshCheckInHarian() {
        String sekarang = tanggalHariIni();
        if (!sekarang.equals(tanggalCheckInAktif)) {
            memberCheckedInHariIni.clear();
            tanggalCheckInAktif = sekarang;
        }
    }

    private String generateMemberId() {
        int max = 0;
        for (Member m : databaseMember) {
            String angka = m.getIdMember().replaceAll("\\D", "");
            if (!angka.isEmpty()) {
                try {
                    max = Math.max(max, Integer.parseInt(angka));
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("MEM-%03d", max + 1);
    }

    private Member cariMemberById(String id) {
        for (Member mem : databaseMember) {
            if (mem.getIdMember().equalsIgnoreCase(id)) return mem;
        }
        return null;
    }

    private void menuCekLapangan() {
        boolean diCek = true;
        while (diCek) {
            clearScreen();
            System.out.println("\n--- STATUS LAPANGAN ---");
            boolean semuaFull = true;
            for (Lapangan lap : daftarLapangan) {
                System.out.print("Lap " + lap.getNomor() + ": ");
                if (lap.getJadwal().isEmpty()) {
                    System.out.println("[KOSONG]");
                    semuaFull = false;
                } else {
                    semuaFull = false;
                    for (int i = 0; i < lap.getJadwal().size(); i++) {
                        Pelanggan p = lap.getJadwal().get(i);
                        if (i > 0) System.out.print("       ");
                        System.out.printf("[%s] %s (%02d.00-%02d.00) Hari: %s | %s%n",
                                p.getStatusTransaksi(), p.getNama(),
                                p.getJamMulai(), p.getJamSelesai(), p.getHariMain(), p.getJenisPelanggan());
                    }
                }
            }

            if (semuaFull) {
                System.out.println("\nSemua lapangan FULL dari Senin sampe Minggu!");
            }

            System.out.println("\n1. Batalkan Booking / Hapus Jadwal");
            System.out.println("0. Kembali ke Menu Utama");
            System.out.print("Pilih: ");
            int pil = inputInt();

            if (pil == 1) {
                System.out.print("\nNama yang dibatalin: ");
                String namaBatal = scanner.nextLine();
                boolean found = false;

                for (Lapangan lap : daftarLapangan) {
                    for (int i = 0; i < lap.getJadwal().size(); i++) {
                        Pelanggan p = lap.getJadwal().get(i);
                        if (p.getNama().equalsIgnoreCase(namaBatal)) {
                            if (p instanceof PelangganBiasa) {
                                PelangganBiasa pb = (PelangganBiasa) p;
                                if (pb.getIsBooking()) {
                                    System.out.printf("\nBooking %s di Lap %d dibatalin.%n", p.getNama(), lap.getNomor());
                                    System.out.printf("DP Rp %,.0f hangus / dikembalikan.%n", pb.hitungDP());
                                } else {
                                    System.out.printf("\nJadwal %s di Lap %d dihapus.%n", p.getNama(), lap.getNomor());
                                }
                            } else if (p instanceof Member) {
                                System.out.printf("\nMember %s dibatalin. Sesi gak balik.%n", p.getNama());
                            }
                            lap.getJadwal().remove(i);
                            jadwalStore.saveJadwal(daftarLapangan);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (!found) System.out.println("\nNama gak ditemukan.");
                System.out.println("\nTekan [ENTER] buat balik...");
                scanner.nextLine();
            } else if (pil == 0) {
                diCek = false;
            }
        }
    }

    private void menuTransaksiBiasa() {
        clearScreen();
        System.out.println("\n--- SEWA / BOOKING LAPANGAN ---");

        String nama;
        while (true) {
            System.out.print("\nNama Pemesan (0 = Kembali): ");
            nama = scanner.nextLine().trim();
            if (nama.equals("0")) return;
            if (nama.isEmpty()) {
                System.out.println("Nama gaboleh kosong!");
                continue;
            }
            break;
        }

        int lama;
        while (true) {
            System.out.print("Lama Sewa (Jam) (0 = Batal): ");
            lama = inputInt();
            if (lama == 0) return;

            int maxLama = JAM_TUTUP - JAM_BUKA;
            if (lama <= 0) {
                System.out.println("DITOLAK! Lama sewa harus lebih dari 0 jam.");
                continue;
            } else if (lama > maxLama) {
                System.out.println("DITOLAK! Lama sewa kegedean.");
                System.out.println("Maksimal " + maxLama + " jam (07.00 - 22.00 WIB).");
                continue;
            }

            break;
        }

        int jenis;
        while (true) {
            System.out.print("Jenis (1. Booking / 2. Datang Langsung / 0. Batal): ");
            jenis = inputInt();
            if (jenis == 0) return;
            if (jenis != 1 && jenis != 2) {
                System.out.println("Jenis transaksi gak valid!");
                continue;
            }
            break;
        }

        boolean isBooking = (jenis == 1);
        String hari = "";

        if (isBooking) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate hariIniLocal = LocalDate.now();
            boolean selesai = false;

            while (!selesai) {
                boolean tanggalValid = false;
                while (!tanggalValid) {
                    System.out.print("Tanggal Main (DD-MM-YYYY): ");
                    hari = scanner.nextLine().trim();

                    try {
                        LocalDate tanggalInput = LocalDate.parse(hari, dateFormatter);
                        
                        if (tanggalInput.isBefore(hariIniLocal)) {
                            System.out.println("DITOLAK! Tanggal udah lewat. Hari ini " + hariIniLocal.format(dateFormatter));
                        } else if (tanggalInput.getYear() > hariIniLocal.getYear() + 1) {
                            System.out.println("DITOLAK! Booking kejauhan. Max tahun " + (hariIniLocal.getYear() + 1));
                        } else {
                            tanggalValid = true;
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Format salah! Contoh: 25-05-2026");
                    }
                }

                int jam = pilihJamDanCekLapangan(hari, lama, isBooking);
                if (jam == -2) return;
                if (jam == -1) {
                    System.out.println("\nLapangan penuh di jam itu buat tanggal " + hari);
                    System.out.print("Mau coba tanggal lain? (Y/N): ");
                    if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;
                    continue;
                }
                
                clearScreen();
                System.out.println("\n--- SEWA / BOOKING LAPANGAN ---");
                System.out.println("Nama: " + nama + " | Tanggal: " + hari + " | Jam: " + jam + ".00");

                PelangganBiasa pBiasa = new PelangganBiasa(nama, hari, jam, lama, isBooking);

                System.out.printf("Total Biaya: Rp %,.0f (30rb/jam)%n", pBiasa.hitungBiaya());
                if (isBooking) {
                    System.out.println("Status: BOOKING (DP 50%)");
                    System.out.printf("DP: Rp %,.0f%n", pBiasa.hitungDP());
                }

                ArrayList<Integer> listLapanganTersedia = new ArrayList<>();
                for (Lapangan lap : daftarLapangan) {
                    if (lap.isTersedia(hari, jam, lama)) {
                        listLapanganTersedia.add(lap.getNomor());
                    }
                }

                if (listLapanganTersedia.isEmpty()) {
                    System.out.println("\nWaduh, lapangan keburu diisi orang lain. Coba tanggal/jam lain.");
                    continue;
                }

                while (true) {
                    System.out.print("\nPilih Lapangan " + listLapanganTersedia + " (0 = Batal): ");
                    int noLap = inputInt();

                    if (noLap == 0) {
                        System.out.println("\nDibatalkan.");
                        return;
                    }

                    if (listLapanganTersedia.contains(noLap)) {
                        Lapangan lapDipilih = daftarLapangan.get(noLap - 1);
                        
                        if (!lapDipilih.isTersedia(hari, jam, lama)) {
                            System.out.println("\nLapangan " + noLap + " udah keisi! Coba yg lain.");
                            continue;
                        }
                        
                        lapDipilih.tambahJadwal(pBiasa);
                        jadwalStore.saveJadwal(daftarLapangan);

                        double bayar = isBooking ? pBiasa.hitungDP() : pBiasa.hitungBiaya();
                        String ket = isBooking ? "DP Booking 50%" : "Lunas";

                        System.out.printf("\nPembayaran Rp %,.0f diterima. Lapangan %d disewa.%n", bayar, noLap);

                        String jadwal = String.format("%02d.00 - %02d.00 WIB", jam, jam + lama);
                        Transaksi trx = new Transaksi(nama, "Pelanggan Biasa", noLap, jadwal, bayar, ket);
                        riwayatTransaksi.add(trx);
                        trx.tampilkanStruk();
                        selesai = true;
                        break;
                    } else {
                        System.out.println("Nomor lapangan gak valid!");
                    }
                }
            }
        } else {
            boolean datangSelesai = false;
            while (!datangSelesai) {
                hari = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                System.out.println("\nTanggal: " + hari + " (Hari Ini)");

                int jamSekarangOtomatis = LocalTime.now().getHour();
                if (jamSekarangOtomatis >= JAM_TUTUP) {
                    System.out.println("\nMaaf, GOR udah tutup (Batas " + JAM_TUTUP + ".00 WIB).");
                    System.out.println("Datang langsung dibatalin. Coba booking aja.");
                    return;
                }

                int jam = pilihJamDanCekLapangan(hari, lama, false);
                if (jam == -2) return;
                if (jam == -1) {
                    System.out.println("\nLapangan penuh di jam itu. Coba jam lain atau besok.");
                    return;
                }

                clearScreen();
                System.out.println("\n--- SEWA / BOOKING LAPANGAN ---");
                System.out.println("Nama: " + nama + " | Tanggal: " + hari + " | Jam: " + jam + ".00");

                PelangganBiasa pBiasa = new PelangganBiasa(nama, hari, jam, lama, isBooking);

                System.out.printf("Total Biaya: Rp %,.0f (30rb/jam)%n", pBiasa.hitungBiaya());

                ArrayList<Integer> listLapanganTersedia = new ArrayList<>();
                for (Lapangan lap : daftarLapangan) {
                    if (lap.isTersedia(hari, jam, lama)) {
                        listLapanganTersedia.add(lap.getNomor());
                    }
                }

                if (listLapanganTersedia.isEmpty()) {
                    System.out.println("\nWaduh, lapangan keburu diisi. Coba jam lain.");
                    System.out.print("Mau coba jam lain? (Y/N): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("Y")) continue;
                    return;
                }

                while (true) {
                    System.out.print("\nPilih Lapangan " + listLapanganTersedia + " (0 = Batal): ");
                    int noLap = inputInt();

                    if (noLap == 0) {
                        System.out.println("\nDibatalkan.");
                        return;
                    }

                    if (listLapanganTersedia.contains(noLap)) {
                        Lapangan lapDipilih = daftarLapangan.get(noLap - 1);
                        
                        if (!lapDipilih.isTersedia(hari, jam, lama)) {
                            System.out.println("\nLapangan " + noLap + " udah keisi! Coba yg lain.");
                            continue;
                        }
                        
                        lapDipilih.tambahJadwal(pBiasa);
                        jadwalStore.saveJadwal(daftarLapangan);

                        double bayar = pBiasa.hitungBiaya();
                        String jadwal = String.format("%02d.00 - %02d.00 WIB", jam, jam + lama);
                        Transaksi trx = new Transaksi(nama, "Pelanggan Biasa", noLap, jadwal, bayar, "Lunas");
                        riwayatTransaksi.add(trx);
                        System.out.printf("\nPembayaran Rp %,.0f diterima. Lapangan %d disewa.%n", bayar, noLap);
                        trx.tampilkanStruk();
                        datangSelesai = true;
                        break;
                    } else {
                        System.out.println("Nomor lapangan gak valid!");
                    }
                }
            }
        }
    }

    private int pilihJamDanCekLapangan(String hari, int lama, boolean isBooking) {
        int jamSekarang = LocalTime.now().getHour();
        
        while (true) {
            System.out.println("\n[GOR Buka: 07.00 - 22.00 WIB]");
            System.out.println("(Format jam: 13.00 / 13:00 / 13)");
            int jam = inputJam("Rencana Main Jam (0 = Batal): ");

            if (jam == 0) return -2;

            if ((jam + lama) > JAM_TUTUP) {
                System.out.println("DITOLAK! GOR tutup jam 22.00 WIB.");
                continue;
            } else if (jam < JAM_BUKA) {
                System.out.println("DITOLAK! GOR buka jam 07.00 WIB.");
                continue;
            } else if (!isBooking && jam < jamSekarang) {
                System.out.println("DITOLAK! Waktu udah lewat. Sekarang jam " + jamSekarang + ".00 WIB.");
                continue;
            }

            if (lama > 5) {
                int sisaJam = JAM_TUTUP - (jam + lama);
                if (sisaJam < 0) sisaJam = 0;
                System.out.print("\nAPAKAH ANDA YAKIN? Waktu yang tersisa setelah sewa adalah " + sisaJam + " jam. (Y/N): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    continue;
                }
            }

            ArrayList<Integer> tersedia = new ArrayList<>();
            for (Lapangan lap : daftarLapangan) {
                if (lap.isTersedia(hari, jam, lama)) {
                    tersedia.add(lap.getNomor());
                }
            }

            if (tersedia.isEmpty()) {
                System.out.println("\nMaaf, semua lapangan penuh di jam " + jam + ".00.");
                System.out.print("Mau coba jam lain? (Y/N): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    if (isBooking) {
                        System.out.print("Atau mau ganti tanggal aja? (Y/N): ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) return -1;
                    }
                    return -2;
                }
            } else {
                return jam;
            }
        }
    }

    private void menuMember() {
        boolean diMember = true;
        while (diMember) {
            clearScreen();
            System.out.println("\n--- MENU MEMBER ---");
            System.out.println("1. Daftar Member Baru");
            System.out.println("2. Check-in (Pakai Sesi)");
            System.out.println("3. Lihat Daftar Member");
            System.out.println("4. Hapus Member");
            System.out.println("5. Reset Sesi (Per Member)");
            System.out.println("0. Kembali ke Menu Utama");
            System.out.print("Pilih: ");
            int pil = inputInt();

            switch (pil) {
                case 1: daftarMemberBaru(); break;
                case 2: menuCheckInMember(); break;
                case 3: tampilkanSemuaMember(); break;
                case 4: hapusMember(); break;
                case 5: resetSesiMember(); break;
                case 0: diMember = false; continue;
                default: System.out.println("Pilihan gak valid.");
            }

            // langsung balik ke menu member tanpa pause
        }
    }

    private void daftarMemberBaru() {
        clearScreen();
        System.out.println("\n--- DAFTAR MEMBER BARU ---");
        System.out.print("\nNama Member: ");
        String nama = scanner.nextLine().trim();
        if (nama.isEmpty()) {
            System.out.println("\nNama gaboleh kosong!");
            return;
        }

        String hari = "";
        while (true) {
            System.out.print("\nHari Main Tetap: ");
            hari = scanner.nextLine().trim();
            if (hari.equalsIgnoreCase("Senin") || hari.equalsIgnoreCase("Selasa") ||
                hari.equalsIgnoreCase("Rabu") || hari.equalsIgnoreCase("Kamis") ||
                hari.equalsIgnoreCase("Jumat") || hari.equalsIgnoreCase("Sabtu") ||
                hari.equalsIgnoreCase("Minggu")) {
                hari = hari.substring(0, 1).toUpperCase() + hari.substring(1).toLowerCase();
                break;
            }
            System.out.println("Hari gak valid! Pilih Senin - Minggu.");
        }

        int jam;
        while (true) {
            System.out.println("\n(Format jam: 13.00 / 13:00 / 13)");
            jam = inputJam("Jam Main Tetap (0 = Batal): ");
            if (jam == 0) return;
            if (jam < JAM_BUKA || (jam + 3) > JAM_TUTUP) {
                System.out.println("\nGAGAL! Jadwal main (3 Jam) melebihi jam operasional.");
                continue;
            }
            break;
        }

        while (true) {
            // Cek bentrok jadwal tetap + max 4 member per lapangan per hari
            int[] countPerLapangan = new int[9];
            for (Member existing : databaseMember) {
                if (existing.getHariTetap().equalsIgnoreCase(hari)) {
                    countPerLapangan[existing.getNomorLapangan()]++;
                }
            }

            ArrayList<Integer> lapanganTersedia = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                if (countPerLapangan[i] < 4) {
                    lapanganTersedia.add(i);
                }
            }

            // Coret lapangan yang jamnya tabrakan
            for (Member existing : databaseMember) {
                if (existing.getHariTetap().equalsIgnoreCase(hari)) {
                    int start1 = jam;
                    int end1 = jam + 3;
                    int start2 = existing.getJamMulai();
                    int end2 = existing.getJamSelesai();
                    
                    if ((start1 >= start2 && start1 < end2) || 
                        (end1 > start2 && end1 <= end2) || 
                        (start1 <= start2 && end1 >= end2)) {
                        lapanganTersedia.remove(Integer.valueOf(existing.getNomorLapangan()));
                    }
                }
            }

            if (lapanganTersedia.isEmpty()) {
                System.out.println("\nGAGAL! Lapangan penuh di jam tersebut buat hari " + hari + ".");
                System.out.print("Mau ganti hari? (Y/N): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                    while (true) {
                        System.out.print("\nHari Main Tetap: ");
                        hari = scanner.nextLine().trim();
                        if (hari.equalsIgnoreCase("Senin") || hari.equalsIgnoreCase("Selasa") ||
                            hari.equalsIgnoreCase("Rabu") || hari.equalsIgnoreCase("Kamis") ||
                            hari.equalsIgnoreCase("Jumat") || hari.equalsIgnoreCase("Sabtu") ||
                            hari.equalsIgnoreCase("Minggu")) {
                            hari = hari.substring(0, 1).toUpperCase() + hari.substring(1).toLowerCase();
                            break;
                        }
                        System.out.println("Hari gak valid! Pilih Senin - Minggu.");
                    }
                    continue;
                }
                return;
            }

            System.out.print("\nPilih Lapangan " + lapanganTersedia + " (0 = Batal): ");
            int noLap = inputInt();
            if (noLap == 0) return;

            if (lapanganTersedia.contains(noLap)) {
                Member m = new Member(nama, generateMemberId(), hari, jam, noLap);
                databaseMember.add(m);
                memberStore.save(databaseMember);

                Transaksi trxMember = new Transaksi(nama, "Member", noLap, "Pendaftaran Member", 300000, "Pendaftaran Member Baru");
                riwayatTransaksi.add(trxMember);
                trxMember.tampilkanStruk();

                System.out.println("\nMember " + m.getIdMember() + " Berhasil Terdaftar!");

                if (hari.equalsIgnoreCase(getHariIni())) {
                    System.out.print("\nLangsung pake 1 sesi hari ini? (Y/N): ");
                    String pakaiSekarang = scanner.nextLine().trim();
                    
                    if (pakaiSekarang.equalsIgnoreCase("Y")) {
                        Lapangan lapTerpilih = daftarLapangan.get(noLap - 1);
                        int jamSekarang = LocalTime.now().getHour();
                        String tglSekarang = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                        
                        if (jam < jamSekarang) {
                            System.out.println("\nGAGAL! Waktu udah lewat. Sekarang jam " + jamSekarang + ".00 WIB.");
                        } 
                        else if (lapTerpilih.isTersedia(tglSekarang, jam, 3)) { 
                            m.gunakanSesi();
                            
                            Member sesiHariIni = new Member(m.getNama(), m.getIdMember(), tglSekarang, jam, noLap);
                            lapTerpilih.tambahJadwal(sesiHariIni);
                            
                            memberCheckedInHariIni.add(m.getIdMember());
                            memberStore.save(databaseMember);
                            jadwalStore.saveJadwal(daftarLapangan); 
                            
                            System.out.println("\nSesi hari ini (" + tglSekarang + ") kepake! Sisa sesi: " + m.getSisaSesi());
                        } else {
                            System.out.println("\nLapangan " + noLap + " lagi dipake orang. Jadwal tetap lu aman.");
                        }
                    }
                }
                break;
            } else {
                System.out.println("\nNomor lapangan gak valid!");
            }
        }
    }

    private void menuCheckInMember() {
        clearScreen();
        refreshCheckInHarian();
        System.out.println("\n--- CHECK-IN MEMBER ---");
        System.out.print("ID Member: ");
        String searchId = scanner.nextLine().trim();

        Member m = cariMemberById(searchId);
        if (m == null) {
            System.out.println("ID gak ditemukan.");
            return;
        }

        boolean isJadwalTetap = m.getHariTetap().equalsIgnoreCase(getHariIni());
        if (!isJadwalTetap) {
            System.out.println("\nGAGAL! Hari ini " + getHariIni() + ", jadwal tetap kamu hari " + m.getHariTetap() + ".");
            System.out.println("Check-in cuma bisa di hari jadwal tetap.");
            return;
        }

        if (memberCheckedInHariIni.contains(m.getIdMember())) {
            System.out.println("\nUdah check-in hari ini!");
            return;
        }
        if (m.getSisaSesi() <= 0) {
            System.out.println("\nSesi udah habis!");
            return;
        }

        Lapangan lapMember = daftarLapangan.get(m.getNomorLapangan() - 1);
        
        String tglSekarang = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        if (!lapMember.isTersedia(tglSekarang, m.getJamMulai(), m.getLamaMain())) {
            System.out.println("\nLapangan lagi dipake orang. Check-in ditolak!");
            return;
        }

        m.gunakanSesi();
        memberStore.save(databaseMember);
        
        Member sesiCheckIn = new Member(m.getNama(), m.getIdMember(), tglSekarang, m.getJamMulai(), m.getNomorLapangan());
        lapMember.tambahJadwal(sesiCheckIn);
        
        memberCheckedInHariIni.add(m.getIdMember());
        jadwalStore.saveJadwal(daftarLapangan); 

        System.out.println("\nCheck-in berhasil!");
        System.out.println("Sisa sesi bulan ini: " + m.getSisaSesi() + " sesi.");
    }

    private void tampilkanSemuaMember() {
        clearScreen();
        System.out.println("\n--- DAFTAR MEMBER ---");
        if (databaseMember.isEmpty()) {
            System.out.println("Belum ada member.");
        }
        for (Member m : databaseMember) {
            System.out.printf("%s | %s | %s | %02d.00 | Lap: %d | Sisa: %d sesi%n",
                    m.getIdMember(), m.getNama(), m.getHariTetap(), m.getJamMulai(), m.getNomorLapangan(), m.getSisaSesi());
        }
    }

    private void hapusMember() {
        clearScreen();
        System.out.println("\n--- HAPUS MEMBER ---");
        System.out.print("ID Member: ");
        String id = scanner.nextLine().trim();
        Member target = cariMemberById(id);
        
        if (target != null) {
            System.out.printf("Yakin hapus %s (%s)? (1. Ya): ", target.getNama(), target.getIdMember());
            if (inputInt() == 1) {
                databaseMember.remove(target);
                memberCheckedInHariIni.remove(target.getIdMember());
                for (Lapangan lap : daftarLapangan) {
                    lap.hapusJadwalMemberById(target.getIdMember());
                }
                memberStore.save(databaseMember);
                jadwalStore.saveJadwal(daftarLapangan);
                System.out.println("Member dihapus.");
            }
        } else {
            System.out.println("ID gak ditemukan.");
        }
    }

    private void resetSesiMember() {
        clearScreen();
        System.out.println("\n--- RESET SESI MEMBER ---");
        if (databaseMember.isEmpty()) {
            System.out.println("Belum ada member.");
            return;
        }
        
        System.out.println("Pilih member yang mau direset sesinya:");
        for (int i = 0; i < databaseMember.size(); i++) {
            Member m = databaseMember.get(i);
            System.out.printf("%d. %s (%s) - Sisa: %d sesi%n", i + 1, m.getNama(), m.getIdMember(), m.getSisaSesi());
        }
        System.out.print("\nNomor member (0 = Batal): ");
        int idx = inputInt();
        if (idx <= 0 || idx > databaseMember.size()) return;
        
        Member target = databaseMember.get(idx - 1);
        System.out.printf("Reset sesi %s? (1. Ya): ", target.getNama());
        if (inputInt() == 1) {
            target.resetSesi();
            memberStore.save(databaseMember);
            System.out.println("Sesi " + target.getNama() + " direset ke 4.");
        }
    }

    private void menuToko() {
        boolean diToko = true;
        while (diToko) {
            clearScreen();
            System.out.println("\n--- TOKO PERLENGKAPAN ---");
            System.out.println("Barang Tersedia:");
            for (int i = 0; i < daftarBarang.size(); i++) {
                Barang b = daftarBarang.get(i);
                System.out.printf("%d. %s (Rp %,.0f/pcs) - Stok: %d%n", (i + 1), b.getNama(), b.getHarga(), b.getStok());
            }

            int pilBarang;
            while (true) {
                System.out.print("\nPilih Barang (1-" + daftarBarang.size() + ") [0 = Kembali]: ");
                pilBarang = inputInt();
                if (pilBarang == 0) { diToko = false; break; }
                if (pilBarang < 1 || pilBarang > daftarBarang.size()) {
                    System.out.println("Pilihan gak valid!");
                    continue;
                }
                break;
            }
            if (!diToko) break;

            Barang brg = daftarBarang.get(pilBarang - 1);
            int qty;
            while (true) {
                System.out.print("\nJumlah (0 = Batal): ");
                qty = inputInt();
                if (qty == 0) break;
                if (qty < 0) {
                    System.out.println("Jumlah gak valid!");
                    continue;
                }
                if (brg.getStok() < qty) {
                    System.out.println("\nStok gak cukup. Stok tersedia: " + brg.getStok());
                    continue;
                }
                double total = brg.getHarga() * qty;
                brg.kurangiStok(qty);
                Transaksi trx = new Transaksi("Tamu", "Toko", 0, brg.getNama() + " x" + qty, total, "Lunas");
                riwayatTransaksi.add(trx);
                System.out.printf("\nTotal: Rp %,.0f%n", total);
                break;
            }
        }
    }

    private void menuLaporanKeuangan() {
        boolean diLaporan = true;
        while (diLaporan) {
            clearScreen();
            System.out.println("\n--- LAPORAN KEUANGAN ---");
            System.out.println("1. Riwayat Transaksi");
            System.out.println("2. Rekap Pendapatan");
            System.out.println("0. Kembali ke Menu Utama");
            System.out.print("Pilih: ");
            int pil = inputInt();

            switch (pil) {
                case 1:
                    clearScreen();
                    System.out.println("\n--- RIWAYAT TRANSAKSI ---");
                    if (riwayatTransaksi.isEmpty()) {
                        System.out.println("\nBelum ada transaksi.");
                    }
                    for (Transaksi t : riwayatTransaksi) {
                        System.out.printf("| %-8s | %-15s | %-14s | Rp %,.0f |%n", 
                                t.getIdTransaksi(), t.getNamaPelanggan(), t.getJenis(), t.getTotalBayar());
                    }
                    System.out.println("\nTekan [ENTER] buat balik...");
                    scanner.nextLine();
                    break;
                case 2:
                    clearScreen();
                    double total = 0;
                    for (Transaksi t : riwayatTransaksi) total += t.getTotalBayar();
                    System.out.printf("\nTOTAL PENDAPATAN: Rp %,.0f%n", total);
                    System.out.println("\nTekan [ENTER] buat balik...");
                    scanner.nextLine();
                    break;
                case 0:
                    diLaporan = false;
                    break;
                default:
                    System.out.println("Pilihan gak valid.");
            }
        }
    }

    private void menuPelunasan() {
        boolean diPelunasan = true;
        while (diPelunasan) {
            clearScreen();
            System.out.println("\n--- PELUNASAN BOOKING ---");
            
            ArrayList<Object[]> bookingBelumLunas = new ArrayList<>();
            for (Lapangan lap : daftarLapangan) {
                for (Pelanggan p : lap.getJadwal()) {
                    if (p instanceof PelangganBiasa) {
                        PelangganBiasa pb = (PelangganBiasa) p;
                        if (pb.getIsBooking()) {
                            String nama = pb.getNama();
                            int noLap = lap.getNomor();
                            double sisa = pb.hitungBiaya() - pb.hitungDP();
                            
                            boolean sudahLunas = false;
                            for (Transaksi t : riwayatTransaksi) {
                                if (t.getNamaPelanggan().equalsIgnoreCase(nama) && t.getKeterangan().equalsIgnoreCase("Pelunasan Booking")) {
                                    sudahLunas = true;
                                    break;
                                }
                            }
                            if (!sudahLunas) {
                                bookingBelumLunas.add(new Object[]{nama, noLap, sisa, pb});
                            }
                        }
                    }
                }
            }

            if (bookingBelumLunas.isEmpty()) {
                System.out.println("\nGak ada booking yang perlu dilunasi.");
                System.out.println("\nTekan [ENTER] buat kembali...");
                scanner.nextLine();
                return;
            }

            System.out.println("\nBooking yang belum lunas:");
            for (int i = 0; i < bookingBelumLunas.size(); i++) {
                Object[] data = bookingBelumLunas.get(i);
                System.out.printf("%d. %s - Lap %d - Sisa: Rp %,.0f%n", i + 1, data[0], data[1], data[2]);
            }

            System.out.print("\nPilih nomor (0 = Kembali): ");
            int idx = inputInt();
            if (idx <= 0 || idx > bookingBelumLunas.size()) continue;

            Object[] dipilih = bookingBelumLunas.get(idx - 1);
            String nama = (String) dipilih[0];
            int noLap = (int) dipilih[1];
            double sisa = (double) dipilih[2];
            PelangganBiasa pb = (PelangganBiasa) dipilih[3];

            System.out.printf("\n%s - Lap %d%n", nama, noLap);
            System.out.printf("Total: Rp %,.0f | DP: Rp %,.0f | Sisa: Rp %,.0f%n", pb.hitungBiaya(), pb.hitungDP(), sisa);
            System.out.print("\nProses pelunasan? (1. Ya / 0. Tidak): ");
            int konfirm = inputInt();
            if (konfirm == 1) {
                String jadwal = String.format("%02d.00 - %02d.00 WIB", pb.getJamMulai(), pb.getJamSelesai());
                Transaksi trx = new Transaksi(nama, "Pelanggan Biasa", noLap, jadwal, sisa, "Pelunasan Booking");
                riwayatTransaksi.add(trx);
                trx.tampilkanStruk();
                System.out.println("\nLunas! Sisa pembayaran Rp " + String.format("%,.0f", sisa) + " diterima.");
            }
            System.out.println("\nTekan [ENTER] buat balik ke menu pelunasan...");
            scanner.nextLine();
        }
    }
}
