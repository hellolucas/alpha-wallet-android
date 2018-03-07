package com.wallet.crypto.alphawallet;

import com.wallet.crypto.alphawallet.entity.TransactionDecoder;
import com.wallet.crypto.alphawallet.entity.TransactionInput;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.math.BigInteger;

/**
 * Created by weiwu on 7/3/18.
 */

public class TransactionDecodingTest {

    String transfer_transaction = "0xa6fb475f000000000000000000000000951c19daead668bfa8391c94286f8ce7cbda2fe3000000000000000000000000879230570f360424bc5baa99906d5f640a75551e000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000004";
    /* James has the following test transaction input without signature. */
    // String transaction_from_james_2 = "0      000000000000000000000000cb53390d32495163936ee451fee7089cd30be33c000000000000000000000000000000000000000000000000000000000000dead000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001";
    String trade_transaction = "0x696ecc55" +                                //trade(uint,[],v,r,s)
        "000000000000000000000000000000000000000000000000000000005a9a00e2" + // expiry
        "00000000000000000000000000000000000000000000000000000000000000a0" + // var args declaration
        "000000000000000000000000000000000000000000000000000000000000001b" + // 27: v
        "c59d6718734043600a49ec2419e566fa03676058e88326ff1161c579c6b8e799" + // R
        "51d22461ab6f27bedd72d6b56c1fecf9f4cbb79a7728c510022617a9e42782ea" + // S
        "000000000000000000000000000000000000000000000000000000000000000a" +
        "0000000000000000000000000000000000000000000000000000000000000009" +
        "000000000000000000000000000000000000000000000000000000000000000a" +
        "000000000000000000000000000000000000000000000000000000000000000b" +
        "000000000000000000000000000000000000000000000000000000000000000c" +
        "000000000000000000000000000000000000000000000000000000000000000d" +
        "000000000000000000000000000000000000000000000000000000000000000e" +
        "000000000000000000000000000000000000000000000000000000000000000f" +
        "0000000000000000000000000000000000000000000000000000000000000010" +
        "0000000000000000000000000000000000000000000000000000000000000011" +
        "0000000000000000000000000000000000000000000000000000000000000012";

    @Test
    public void TransactionInputCanBeDecoded() {
        TransactionInput i;
        TransactionDecoder t = new TransactionDecoder();
        i = t.decodeInput(transfer_transaction);
        assertEquals("0x951c19daead668bfa8391c94286f8ce7cbda2fe3", i.getFirstAddress());
        i = t.decodeInput(trade_transaction);
        assertEquals(BigInteger.valueOf(0x1b), i.sigData.get(0));
    }
}
