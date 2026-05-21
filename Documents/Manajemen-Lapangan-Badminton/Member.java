public class Member extends Pelanggan {
    private static final double HARGA_BULANAN = 300000;
    private static final int LAMA_MAIN_FIX = 3;

    private final String idMember;
    private final String hariTetap;
    private int sisaSesi;
    private final int nomorLapangan;

    public Member(String nama, String idMember, String hariTetap, int jamMulai, int nomorLapangan) {
        super(nama, jamMulai, LAMA_MAIN_FIX, "Member");
        this.idMember = idMember;
        this.hariTetap = hariTetap;
        this.nomorLapangan = nomorLapangan;
        this.sisaSesi = 4;
    }

    public String getIdMember() {
        return idMember;
    }

    public String getHariTetap() {
        return hariTetap;
    }

    public int getSisaSesi() {
        return sisaSesi;
    }

    public int getNomorLapangan() {
        return nomorLapangan;
    }

    public void gunakanSesi() {
        if (sisaSesi > 0) {
            sisaSesi--;
        }
    }

    public void setSisaSesi(int sisaSesi) {
        this.sisaSesi = sisaSesi;
    }

    public void resetSesi() {
        this.sisaSesi = 4;
    }

    @Override
    public double hitungBiaya() {
        return HARGA_BULANAN;
    }

    @Override
    public String getStatusTransaksi() {
        return "MEMBER AKTIF";
    }
}
