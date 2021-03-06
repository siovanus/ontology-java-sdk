/*
 * Copyright (C) 2018 The ontology Authors
 * This file is part of The ontology library.
 *
 *  The ontology is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The ontology is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with The ontology.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.ontio.crypto.sm;

import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.BigIntegers;

public class SM2Utils {

    public static void generateKeyPair() {
        SM2 sm2 = SM2.Instance();
        AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ();
    }

    public static ECPoint generatePubkey(byte[] prikey) {
        FixedPointCombMultiplier mul = new FixedPointCombMultiplier();
        SM2 sm2 = SM2.Instance();
        return sm2.ecc_point_g.multiply(new BigInteger(1, prikey)).normalize();
    }
    public static ECPoint decodePoint(byte[] pubkey){
        SM2 sm2 = SM2.Instance();
        return sm2.ecc_curve.decodePoint(pubkey);
    }
    public static boolean verifySignature(ECPoint pubkey, byte[] data, byte[] signature) {
        SM2 sm2 = SM2.Instance();
        SM2Signer signer = new SM2Signer();

        signer.init(false, new ECPublicKeyParameters(pubkey, new ECDomainParameters(sm2.ecc_curve, sm2.ecc_point_g, sm2.ecc_n)));

        BigInteger[] d = new BigInteger[2];
        try {
            signer.update(data, 0, data.length);
            byte[] r = new byte[32];
            byte[] s = new byte[32];
            System.arraycopy(signature, 0, r, 0, 32);
            System.arraycopy(signature, 32, s, 0, 32);
            boolean success = signer.verifySignature(new BigInteger(1, r), new BigInteger(1, s));
            return success;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] generateSignature(byte[] prikey, byte[] data) {

        SM2 sm2 = SM2.Instance();
        SM2Signer signer = new SM2Signer();
        signer.init(true, new ECPrivateKeyParameters(new BigInteger(1, prikey), new ECDomainParameters(sm2.ecc_curve, sm2.ecc_point_g, sm2.ecc_n)));

        byte[] signature = null;
        BigInteger[] d = new BigInteger[2];
        try {
            signer.update(data, 0, data.length);
            signature = signer.generateSignature();
            d = signer.derDecode(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] signature2 = new byte[64];

        System.arraycopy(BigIntegers.asUnsignedByteArray(32, d[0]), 0, signature2, 0, 32);
        System.arraycopy(BigIntegers.asUnsignedByteArray(32, d[1]), 0, signature2, 32, 32);
        return signature2;
    }

}
