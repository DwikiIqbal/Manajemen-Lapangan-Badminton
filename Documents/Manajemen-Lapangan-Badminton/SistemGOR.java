import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

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

            System.out.print("Pilih menu (1-6): ");
            int pilihan = inputInt();

            switch (pilihan) {
                case 1: menuCekLapangan(); break;
                case 2: menuTransaksiBiasa(); break;
                case 3: menuMember(); break;
                case 4: menuToko(); break;
                case 5: menuLaporanKeuangan(); break;
                case 6:
                    System.out.println("\nTerima kasih telah menggunakan Sistem Manajemen GOR.");
                    System.out.println("Data Member & Jadwal telah tersimpan otomatis.");
                    System.out.println("Exiting program...\n");
                    isRunning = false;
                    break;
                default:
                    System.out.println("\nPilihan tidak valid. Silakan pilih 1-6.");
            }

            if (isRunning) pause();
        }

        memberStore.save(databaseMember);
    }

    private void inisialisasiData() {
        clearScreen();
        System.out.println("=====================================================");
        System.out.println("      SISTEM MANAJEMEN GOR SILMA BADMINTON");
        System.out.println("          Lokasi: Bekasi, West Java");
        System.out.println("=====================================================");
        System.out.println("--- INISIALISASI SISTEM ---");

        databaseMember.addAll(memberStore.load());
        tanggalCheckInAktif = tanggalHariIni();

        for (int i = 1; i <= 8; i++) {
            daftarLapangan.add(new Lapangan(i));
        }
        System.out.println("[INFO] 8 Lapangan Siap Digunakan.");
        
        // Memuat backup jadwal dari TXT (Fitur Anti-Amnesia saat PC Restart)
        jadwalStore.loadJadwal(daftarLapangan);
        System.out.println("[INFO] Data Jadwal Lapangan Berhasil Dimuat.");

        daftarBarang.add(new Barang("Shuttlecock", 10000, 50));
        daftarBarang.add(new Barang("Air Mineral", 5000, 100));
        daftarBarang.add(new Barang("Sewa Sepatu", 20000, 10));
        System.out.println("[INFO] Stok Barang Toko Berhasil Dimuat.");

        System.out.println("\n-----------------------------------------------------");
        System.out.println("[INFO] Sistem siap digunakan untuk hari " + getHariIni() + ".");
        System.out.println("-----------------------------------------------------\n");
    }

    // Mengambil hari secara otomatis dari sistem operasi (Fitur Anti Time-Freeze)
    private String getHariIni() {
        return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
    }

    private void tampilkanHeader() {
        System.out.println("=====================================================");
        System.out.println("      SISTEM MANAJEMEN GOR SILMA BADMINTON");
        System.out.println("          Lokasi: Bekasi, West Java");
        System.out.println("          Hari  : " + getHariIni());
        System.out.println("=====================================================");
        System.out.println("1. Cek Ketersediaan Lapangan");
        System.out.println("2. Transaksi Sewa / Booking (Pelanggan Biasa)");
        System.out.println("3. Pendaftaran / Penggunaan Member");
        System.out.println("4. Kasir Toko Perlengkapan");
        System.out.println("5. Laporan Keuangan & Riwayat");
        System.out.println("6. Keluar");
        System.out.println("=====================================================");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void pause() {
        System.out.println("\nTekan [ENTER] untuk kembali ke menu utama...");
        scanner.nextLine();
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
        System.out.println("\n[STATUS LAPANGAN SAAT INI]");
        for (Lapangan lap : daftarLapangan) {
            System.out.print("- Lapangan " + lap.getNomor() + ": ");
            if (lap.getJadwal().isEmpty()) {
                System.out.println("[TERSEDIA]");
            } else {
                for (int i = 0; i < lap.getJadwal().size(); i++) {
                    Pelanggan p = lap.getJadwal().get(i);
                    if (i > 0) System.out.print("               ");
                    System.out.printf("[%s] - %s (%02d.00 - %02d.00) | %s%n",
                            p.getStatusTransaksi(), p.getNama(),
                            p.getJamMulai(), p.getJamSelesai(), p.getJenisPelanggan());
                }
            }
        }

        System.out.println("\n-----------------------------------------------------");
        System.out.println("1. Batalkan Booking / Hapus Jadwal");
        System.out.println("2. Kembali");
        System.out.print("Pilih: ");
        int pil = inputInt();

        if (pil == 1) {
            System.out.print("Masukkan Nama yang akan dibatalkan: ");
            String namaBatal = scanner.nextLine();
            boolean found = false;

            for (Lapangan lap : daftarLapangan) {
                for (int i = 0; i < lap.getJadwal().size(); i++) {
                    Pelanggan p = lap.getJadwal().get(i);
                    if (p.getNama().equalsIgnoreCase(namaBatal)) {
                        if (p instanceof PelangganBiasa) {
                            PelangganBiasa pb = (PelangganBiasa) p;
                            if (pb.getIsBooking()) {
                                System.out.printf("\n[INFO] Booking %s di Lapangan %d dibatalkan.%n", p.getNama(), lap.getNomor());
                                System.out.printf("[INFO] DP sebesar Rp %,.0f dapat dikembalikan/dihanguskan.%n", pb.hitungDP());
                            } else {
                                System.out.printf("\n[INFO] Jadwal %s di Lapangan %d dihapus.%n", p.getNama(), lap.getNomor());
                            }
                        } else if (p instanceof Member) {
                            System.out.printf("\n[INFO] Member %s dibatalkan. Sesi tidak dikembalikan.%n", p.getNama());
                        }
                        lap.getJadwal().remove(i);
                        jadwalStore.saveJadwal(daftarLapangan); // AUTO SAVE JADWAL SETELAH DIHAPUS
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) System.out.println("[ERROR] Nama tidak ditemukan di jadwal manapun.");
        }
    }

    private void menuTransaksiBiasa() {
        System.out.println("\n--- TRANSAKSI PELANGGAN BIASA ---");
        System.out.print("Nama Penanggung Jawab : ");
        String nama = scanner.nextLine().trim();

        if (nama.isEmpty()) {
            System.out.println("\nNama tidak boleh kosong!");
            return;
        }

        System.out.print("Lama Sewa (Jam)       : ");
        int lama = inputInt();
        if (lama <= 0) {
            System.out.println("\nTRANSAKSI DITOLAK! Lama sewa harus lebih dari 0 jam.");
            return;
        }

        System.out.print("Jenis (1. Booking / 2. Datang Langsung): ");
        int jenis = inputInt();
        if (jenis != 1 && jenis != 2) {
            System.out.println("\nJenis transaksi tidak valid!");
            return;
        }

        boolean isBooking = (jenis == 1);
        boolean jamValidDanTersedia = false;
        int jam = 0;
        ArrayList<Integer> listLapanganTersedia = new ArrayList<>();

        while (!jamValidDanTersedia) {
            System.out.println("\n[INFO] GOR Buka: 07.00 - 22.00 WIB");
            System.out.print("Rencana Main Jam (Format 24 Jam, cth: 19) [Ketik 0 utk Batal]: ");
            jam = inputInt();

            if (jam == 0) {
                System.out.println("\nTRANSAKSI DIBATALKAN oleh pengguna.");
                return;
            }

            if ((jam + lama) > JAM_TUTUP) {
                System.out.printf("\n[DITOLAK] GOR sudah tutup pukul 22.00 WIB.%n");
                continue;
            } else if (jam < JAM_BUKA) {
                System.out.printf("\n[DITOLAK] GOR baru buka mulai pukul 07.00 WIB.%n");
                continue;
            }

            listLapanganTersedia.clear();
            for (Lapangan lap : daftarLapangan) {
                if (lap.isTersedia(jam, lama)) {
                    listLapanganTersedia.add(lap.getNomor());
                }
            }

            if (listLapanganTersedia.isEmpty()) {
                System.out.printf("\n[MOHON MAAF] Semua lapangan sudah penuh pada jam tersebut.%n");
            } else {
                jamValidDanTersedia = true;
            }
        }

        PelangganBiasa pBiasa = new PelangganBiasa(nama, jam, lama, isBooking);

        System.out.printf("Total Biaya : Rp %,.0f (30rb/jam)%n", pBiasa.hitungBiaya());
        if (isBooking) {
            System.out.println("Status      : BOOKING (Wajib DP 50%)");
            System.out.printf("Tagihan DP  : Rp %,.0f%n", pBiasa.hitungDP());
        }

        while (true) {
            System.out.print("\nPilih Nomor Lapangan " + listLapanganTersedia + " (Atau ketik 0 untuk Batal): ");
            int noLap = inputInt();

            if (noLap == 0) {
                System.out.println("\nTRANSAKSI DIBATALKAN oleh pengguna.");
                return;
            }

            if (listLapanganTersedia.contains(noLap)) {
                Lapangan lapDipilih = daftarLapangan.get(noLap - 1);
                lapDipilih.tambahJadwal(pBiasa);
                jadwalStore.saveJadwal(daftarLapangan); // AUTO SAVE TRANSAKSI BARU

                double bayar = isBooking ? pBiasa.hitungDP() : pBiasa.hitungBiaya();
                String ket = isBooking ? "DP Booking 50%" : "Lunas Datang Langsung";

                System.out.printf("\n[INFO] Pembayaran Rp %,.0f diterima. Lapangan %d disewa.%n", bayar, noLap);

                String jadwal = String.format("%02d.00 - %02d.00 WIB", jam, jam + lama);
                Transaksi trx = new Transaksi(nama, "Pelanggan Biasa", noLap, jadwal, bayar, ket);
                riwayatTransaksi.add(trx);
                trx.tampilkanStruk();
                break;
            } else {
                System.out.println("\n[ERROR] Nomor lapangan tidak valid!");
            }
        }
    }

    private void menuMember() {
        System.out.println("\n--- MENU MEMBER GOR ---");
        System.out.println("1. Daftar Member Baru");
        System.out.println("2. Gunakan Sesi Main (Check-in)");
        System.out.println("3. Lihat Daftar Member");
        System.out.println("4. Hapus Member");
        System.out.println("5. Reset Sesi Bulanan (Admin Only)");
        System.out.print("Pilih: ");
        int pil = inputInt();

        switch (pil) {
            case 1: daftarMemberBaru(); break;
            case 2: menuCheckInMember(); break;
            case 3: tampilkanSemuaMember(); break;
            case 4: hapusMember(); break;
            case 5: resetSesiMember(); break;
            default: System.out.println("Pilihan tidak valid.");
        }
    }

    private void daftarMemberBaru() {
        System.out.println("\n--- PENDAFTARAN MEMBER ---");
        System.out.print("Nama Member     : ");
        String nama = scanner.nextLine().trim();
        System.out.print("Hari Main Tetap : ");
        String hari = scanner.nextLine().trim();
        System.out.print("Jam Main Tetap  : ");
        int jam = inputInt();

        if (jam < JAM_BUKA || (jam + 3) > JAM_TUTUP) {
            System.out.println("\nPENDAFTARAN GAGAL! Jadwal main (3 Jam) melewati jam operasional.");
            return;
        }

        ArrayList<Integer> lapanganTersedia = new ArrayList<>();
        for (Lapangan lap : daftarLapangan) {
            if (lap.isTersedia(jam, 3)) lapanganTersedia.add(lap.getNomor());
        }

        if (lapanganTersedia.isEmpty()) {
            System.out.println("\n[GAGAL] Lapangan penuh di jam tersebut.");
            return;
        }

        System.out.print("\nPilih Lapangan Tetap " + lapanganTersedia + " (0 = Batal): ");
        int noLap = inputInt();
        if (noLap == 0) return;

        if (lapanganTersedia.contains(noLap)) {
            Member m = new Member(nama, generateMemberId(), hari, jam, noLap);
            databaseMember.add(m);
            memberStore.save(databaseMember);
            System.out.println("\n[INFO] Member " + m.getIdMember() + " Berhasil Terdaftar!");

            // FITUR: OPSI LANGSUNG MAIN HARI INI
            System.out.print("\nApakah member ingin langsung menggunakan 1 sesi hari ini? (Y/N): ");
            String pakaiSekarang = scanner.nextLine().trim();
            
            if (pakaiSekarang.equalsIgnoreCase("Y")) {
                Lapangan lapTerpilih = daftarLapangan.get(noLap - 1);
                
                if (lapTerpilih.isTersedia(jam, 3)) {
                    m.gunakanSesi();
                    lapTerpilih.tambahJadwal(m);
                    memberCheckedInHariIni.add(m.getIdMember());
                    
                    memberStore.save(databaseMember);
                    jadwalStore.saveJadwal(daftarLapangan); 
                    
                    System.out.println("[SUKSES] Sesi hari ini digunakan! Sisa sesi: " + m.getSisaSesi());
                } else {
                    System.out.println("[GAGAL] Lapangan sedang dipakai hari ini. Tidak bisa langsung main.");
                }
            }
        }
    }

    private void menuCheckInMember() {
        refreshCheckInHarian();
        System.out.print("\nMasukkan ID Member: ");
        String searchId = scanner.nextLine().trim();

        Member m = cariMemberById(searchId);
        if (m == null) {
            System.out.println("[ERROR] ID tidak ditemukan.");
            return;
        }

        // FITUR: SESI FLEKSIBEL
        boolean isJadwalTetap = m.getHariTetap().equalsIgnoreCase(getHariIni());
        if (!isJadwalTetap) {
            System.out.println("\n[INFO] Anda check-in di luar jadwal tetap (" + m.getHariTetap() + "). Sesi fleksibel akan digunakan.");
        }

        if (memberCheckedInHariIni.contains(m.getIdMember())) {
            System.out.println("\n[ACCESS DENIED] Sudah check-in hari ini.");
            return;
        }
        if (m.getSisaSesi() <= 0) {
            System.out.println("\n[ACCESS DENIED] Sesi Anda sudah habis.");
            return;
        }

        Lapangan lapMember = daftarLapangan.get(m.getNomorLapangan() - 1);
        if (!lapMember.isTersedia(m.getJamMulai(), m.getLamaMain())) {
            System.out.println("\n[WARNING] Lapangan sedang dipakai. Harap lapor Admin.");
            return;
        }

        m.gunakanSesi();
        memberStore.save(databaseMember);
        lapMember.tambahJadwal(m);
        memberCheckedInHariIni.add(m.getIdMember());
        jadwalStore.saveJadwal(daftarLapangan); // AUTO SAVE JADWAL

        System.out.println("\n[INFO] Check-in berhasil!");
        System.out.println("[INFO] Sisa sesi main Anda bulan ini: " + m.getSisaSesi() + " Sesi.");
    }

    private void tampilkanSemuaMember() {
        System.out.println("\n--- DAFTAR SEMUA MEMBER ---");
        for (Member m : databaseMember) {
            System.out.printf("%s | %s | %s | %02d.00 | Lap: %d | Sisa: %d%n",
                    m.getIdMember(), m.getNama(), m.getHariTetap(), m.getJamMulai(), m.getNomorLapangan(), m.getSisaSesi());
        }
    }

    private void hapusMember() {
        System.out.print("\nMasukkan ID Member yang dihapus: ");
        String id = scanner.nextLine().trim();
        Member target = cariMemberById(id);
        
        if (target != null) {
            databaseMember.remove(target);
            memberCheckedInHariIni.remove(target.getIdMember());
            for (Lapangan lap : daftarLapangan) {
                lap.hapusJadwalMemberById(target.getIdMember());
            }
            memberStore.save(databaseMember);
            jadwalStore.saveJadwal(daftarLapangan);
            System.out.println("[INFO] Member dihapus.");
        }
    }

    private void resetSesiMember() {
        System.out.print("\nYakin reset semua sesi member? (1. Ya): ");
        if (inputInt() == 1) {
            for (Member m : databaseMember) m.resetSesi();
            memberStore.save(databaseMember);
            System.out.println("[INFO] Sesi direset.");
        }
    }

    private void menuToko() {
        System.out.println("\n--- KASIR TOKO BADMINTON ---");
        System.out.println("Daftar Barang Tersedia:");
        for (int i = 0; i < daftarBarang.size(); i++) {
            Barang b = daftarBarang.get(i);
            System.out.printf("%d. %s (Rp %,.0f/pcs) - Stok: %d%n", (i + 1), b.getNama(), b.getHarga(), b.getStok());
        }

        System.out.print("\nPilih Barang (1-" + daftarBarang.size() + ") [0 utk batal]: ");
        int pilBarang = inputInt();
        if (pilBarang == 0 || pilBarang > daftarBarang.size()) return;

        System.out.print("Jumlah Beli : ");
        int qty = inputInt();

        Barang brg = daftarBarang.get(pilBarang - 1);
        if (brg.getStok() >= qty) {
            double total = brg.getHarga() * qty;
            brg.kurangiStok(qty);
            Transaksi trx = new Transaksi("Tamu", "Toko", 0, brg.getNama() + " x" + qty, total, "Lunas");
            riwayatTransaksi.add(trx);
            System.out.printf("\n[SUKSES] Total Belanja: Rp %,.0f%n", total);
        } else {
            System.out.println("\n[GAGAL] Stok tidak mencukupi.");
        }
    }

    private void menuLaporanKeuangan() {
        System.out.println("\n--- LAPORAN KEUANGAN & RIWAYAT ---");
        System.out.println("1. Riwayat Transaksi Hari Ini");
        System.out.println("2. Rekap Pendapatan");
        System.out.print("Pilih: ");
        int pil = inputInt();

        if (pil == 1) {
            System.out.println("\n--- RIWAYAT TRANSAKSI ---");
            for (Transaksi t : riwayatTransaksi) {
                System.out.printf("| %-8s | %-15s | %-14s | Rp %,.0f |%n", 
                        t.getIdTransaksi(), t.getNamaPelanggan(), t.getJenis(), t.getTotalBayar());
            }
        } else if (pil == 2) {
            double total = 0;
            for (Transaksi t : riwayatTransaksi) total += t.getTotalBayar();
            System.out.printf("\n[TOTAL PENDAPATAN HARI INI] Rp %,.0f%n", total);
        }
    }
}
