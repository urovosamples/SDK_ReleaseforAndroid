
package com.example.piccmanager;

public class CardType {

    private static final int sak_ul = 0x00;
    private static final int sak_ulc = 0x00;
    private static final int sak_mini = 0x09;
    private static final int sak_mfc_1k = 0x08;
    private static final int sak_mfc_4k = 0x18;
    private static final int sak_mfp_2k_sl1 = 0x08;
    private static final int sak_mfp_4k_sl1 = 0x18;
    private static final int sak_mfp_2k_sl2 = 0x10;
    private static final int sak_mfp_4k_sl2 = 0x11;
    private static final int sak_mfp_2k_sl3 = 0x20;
    private static final int sak_mfp_4k_sl3 = 0x20;
    private static final int sak_desfire = 0x20;
    private static final int sak_jcop = 0x28;

    private static final int atqa_ul = 0x4400;
    private static final int atqa_ulc = 0x4400;
    private static final int atqa_mfc = 0x0200;
    private static final int atqa_mfp_s = 0x0400;
    private static final int atqa_mfp_x = 0x4200;
    private static final int atqa_desfire = 0x4403;
    private static final int atqa_jcop = 0x0400;
    private static final int atqa_mini = 0x0400;

    private static final int mifare_ultralight = 0x01;
    private static final int mifare_ultralight_c = 0x02;
    private static final int mifare_classic = 0x03;
    private static final int mifare_classic_1k = 0x04;
    private static final int mifare_classic_4k = 0x05;
    private static final int mifare_plus = 0x06;
    private static final int mifare_plus_2k_sl1 = 0x07;
    private static final int mifare_plus_4k_sl1 = 0x08;
    private static final int mifare_plus_2k_sl2 = 0x09;
    private static final int mifare_plus_4k_sl2 = 0x0A;
    private static final int mifare_plus_2k_sl3 = 0x0B;
    private static final int mifare_plus_4k_sl3 = 0x0C;
    private static final int mifare_desfire = 0x0D;
    private static final int jcop = 0x0F;
    private static final int mifare_mini = 0x10;


    private static final int CONTACTLESS_CARD_TYPE_A_CPU = 0x0000;
    private static final int CONTACTLESS_CARD_TYPE_B_CPU = 0x0100;
    private static final int CONTACTLESS_CARD_TYPE_A_CLASSIC_MINI = 0x0001;
    private static final int CONTACTLESS_CARD_TYPE_A_CLASSIC_1K = 0x0002;
    private static final int CONTACTLESS_CARD_TYPE_A_CLASSIC_4K = 0x0003;
    private static final int CONTACTLESS_CARD_TYPE_A_UL_64 = 0x0004;
    private static final int CONTACTLESS_CARD_TYPE_A_UL_192 = 0x0005;
    private static final int CONTACTLESS_CARD_TYPE_A_MP_2K_SL1 = 0x0006;
    private static final int CONTACTLESS_CARD_TYPE_A_MP_4K_SL1 = 0x0007;
    private static final int CONTACTLESS_CARD_TYPE_A_MP_2K_SL2 = 0x0008;
    private static final int CONTACTLESS_CARD_TYPE_A_MP_4K_SL2 = 0x0009;
    private static final int CONTACTLESS_CARD_UNKNOWN = 0x00FF;

    public static int getCardType(byte[] card, byte cardType) {
        int ret = 0;
        int sak = card[0];
        int pAtqa = card[1];
        int pAtqb = card[2];
        int detectedCard = CONTACTLESS_CARD_UNKNOWN;
        int sakAtqa = sak << 24 | pAtqa << 8 | pAtqb;
        android.util.Log.d("CardType", "%s pHasMoreCards = %d sakAtqa= %d detectedCard= %d"
                + sakAtqa + detectedCard);

        sakAtqa &= 0xFFFF0FFF;
        android.util.Log.d("CardType", " sakAtqa = %d \n" + sakAtqa);
        // Detect mini or classic
        switch (sakAtqa) {
            case sak_mfc_1k << 24 | atqa_mfc:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_CLASSIC_1K;
                break;
            case sak_mfc_4k << 24 | atqa_mfc:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_CLASSIC_4K;
                break;
            case sak_mfp_2k_sl1 << 24 | atqa_mfp_s:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL1;
                break;
            case sak_mini << 24 | atqa_mini:
                // detected_card &= mifare_mini;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_CLASSIC_MINI;
                break;
            case sak_mfp_4k_sl1 << 24 | atqa_mfp_s:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_4K_SL1;
                break;
            case sak_mfp_2k_sl1 << 24 | atqa_mfp_x:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL2;
                break;
            case sak_mfp_4k_sl1 << 24 | atqa_mfp_x:
                // detected_card &= mifare_classic;
                detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_4K_SL2;
                break;
            default:
                break;
        }
        android.util.Log.d("CardType", "detectedCard= %d\n" + detectedCard);
        if (detectedCard == 0x00FF) {
            sakAtqa = sak << 24 | pAtqa << 8 | pAtqb;
            switch (sakAtqa) {
                case sak_ul << 24 | atqa_ul:
                    // detected_card &= mifare_ultralight;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_UL_64;
                    break;
                case sak_mfp_2k_sl2 << 24 | atqa_mfp_s:
                    detectedCard &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL1;
                    break;
                case sak_mfp_2k_sl3 << 24 | atqa_mfp_s:
                    // detected_card &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL1;
                    break;
                case sak_mfp_4k_sl2 << 24 | atqa_mfp_s:
                    // detected_card &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_4K_SL1;
                    break;
                case sak_mfp_2k_sl2 << 24 | atqa_mfp_x:
                    // detected_card &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL2;
                    break;
                case sak_mfp_2k_sl3 << 24 | atqa_mfp_x:
                    // detected_card &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_2K_SL2;
                    break;
                case sak_mfp_4k_sl2 << 24 | atqa_mfp_x:
                    // detected_card &= mifare_plus;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_MP_4K_SL2;
                    break;
                case sak_desfire << 24 | atqa_desfire:
                    // detected_card &= mifare_desfire;
                    detectedCard &= CONTACTLESS_CARD_TYPE_A_UL_192;
                    break;
                case sak_jcop << 24 | atqa_jcop:  // cpu card
                    // detected_card &= jcop;
                    if (cardType == 'A') {
                        detectedCard &= CONTACTLESS_CARD_TYPE_A_CPU;
                    } else if (cardType == 'B') {
                        detectedCard &= CONTACTLESS_CARD_TYPE_B_CPU;
                    } else {
                        detectedCard &= CONTACTLESS_CARD_UNKNOWN;
                    }
                    break;
                default:
                    break;
            }
        }
        if (detectedCard == 0x00FF) {
            if (cardType == 'A') {
                detectedCard &= CONTACTLESS_CARD_TYPE_A_CPU;
            } else if (cardType == 'B') {
                detectedCard &= CONTACTLESS_CARD_TYPE_B_CPU;
            } else {
                detectedCard &= CONTACTLESS_CARD_UNKNOWN;
            }
        }
        android.util.Log.d("CardType", "detectedCard===== %d\n" + detectedCard);
        return detectedCard;
    }
}
