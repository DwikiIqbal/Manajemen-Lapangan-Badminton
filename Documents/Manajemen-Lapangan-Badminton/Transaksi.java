import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaksi {
    private static int counter = 1;

    private final String idTransaksi;
    private final String namaPelanggan;
    private final String jenis;
    private final int nomorLapangan;
    private final String jadwalMain;
    private final double totalBayar;
    private final String keterangan;
    private final String waktuTransaksi;

    public Transaksi(String namaPelanggan, String jenis, int nomorLapangan,
                     String jadwalMain, double totalBayar, String keterangan) {
        this.idTransaksi = "TRX-" + String.format("%04d", counter++);
        this.namaPelanggan = namaPelanggan;
        this.jenis = jenis;
        this.nomorLapangan = nomorLapangan;
        this.jadwalMain = jadwalMain;
        this.totalBayar = totalBayar;
        this.keterangan = keterangan;
        this.waktuTransaksi = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public String getNamaPelanggan() {
        return namaPelanggan;
    }

    public String getJenis() {
        return jenis;
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void tampilkanStruk() {
        System.out.println("\n+===================================================+");
        System.out.println("|           STRUK TRANSAKSI GOR SILMA               |");
        System.out.println("+===================================================+");
        System.out.printf("| ID Transaksi : %-34s |%n", idTransaksi);
        System.out.printf("| Waktu        : %-34s |%n", waktuTransaksi);
        System.out.println("+---------------------------------------------------+");
        System.out.printf("| Nama         : %-34s |%n", namaPelanggan);
        System.out.printf("| Jenis        : %-34s |%n", jenis);
        System.out.printf("| Lapangan     : %-34s |%n", nomorLapangan == 0 ? "-" : "Lapangan " + nomorLapangan);
        System.out.printf("| Jadwal/Item  : %-34s |%n", jadwalMain);
        System.out.println("+---------------------------------------------------+");
        System.out.printf("| Total Bayar  : %-34s |%n", 
        totalBayar == 0 ? "FREE (Member)" : String.format("Rp %,.0f", totalBayar));
        System.out.printf("| Keterangan   : %-34s |%n", keterangan);
        System.out.println("+===================================================+");
        System.out.println("|     Terima kasih telah bertransaksi di GOR        |");
        System.out.println("|              Silma Badminton                      |");
        System.out.println("+===================================================+");
    }
}
