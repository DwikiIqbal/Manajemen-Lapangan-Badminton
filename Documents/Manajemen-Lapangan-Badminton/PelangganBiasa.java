public class PelangganBiasa extends Pelanggan {
    private static final double HARGA_PER_JAM = 30000;
    private final boolean isBooking;

    // Tambahkan String hariMain di sini
    public PelangganBiasa(String nama, String hariMain, int jamMulai, int lamaMain, boolean isBooking) {
        super(nama, hariMain, jamMulai, lamaMain, "Pelanggan Biasa"); 
        this.isBooking = isBooking;
    }

    @Override
    public double hitungBiaya() {
        return lamaMain * HARGA_PER_JAM;
    }

    @Override
    public String getStatusTransaksi() {
        return isBooking ? "BOOKING" : "DATANG LANGSUNG";
    }

    public double hitungDP() {
        return hitungBiaya() / 2.0;
    }

    public boolean getIsBooking() {
        return isBooking;
    }
}
